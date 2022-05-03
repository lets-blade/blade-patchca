package com.hellokaton.blade.patchca;

import com.hellokaton.blade.mvc.WebContext;
import com.hellokaton.blade.mvc.http.Request;
import com.hellokaton.blade.mvc.http.Response;
import com.hellokaton.blade.mvc.http.Session;
import com.hellokaton.blade.server.NettyHttpConst;
import com.hellokaton.blade.server.ProgressiveFutureListener;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import lombok.extern.slf4j.Slf4j;
import org.patchca.color.ColorFactory;
import org.patchca.filter.FilterFactory;
import org.patchca.filter.predefined.DiffuseRippleFilterFactory;
import org.patchca.service.ConfigurableCaptchaService;
import org.patchca.utils.encoder.EncoderHelper;
import org.patchca.word.RandomWordFactory;
import org.patchca.word.WordFactory;

import java.awt.*;
import java.io.*;
import java.util.Random;

/**
 * DefaultPatchca
 */
@Slf4j
public class DefaultPatchca implements Patchca {

    private static final String DEFAULT_SESSION_KEY = "patchca_code";

    private static final Random RANDOM = new Random();

    private final ConfigurableCaptchaService cs;
    private final RandomWordFactory wf;

    public DefaultPatchca() {
        this(x -> {
            int[] c = new int[3];
            int i = RANDOM.nextInt(c.length);
            for (int fi = 0; fi < c.length; fi++) {
                if (fi == i) {
                    c[fi] = RANDOM.nextInt(71);
                } else {
                    c[fi] = RANDOM.nextInt(256);
                }
            }
            return new Color(c[0], c[1], c[2]);
        });
    }

    public DefaultPatchca(ColorFactory colorFactory) {
        cs = new ConfigurableCaptchaService();
        cs.setColorFactory(colorFactory);
        wf = new RandomWordFactory();
        wf.setCharacters("23456789abcdefghigkmnpqrstuvwxyzABCDEFGHIGKLMNPQRSTUVWXYZ");
        wf.setMinLength(4);
        wf.setMaxLength(6);
        cs.setWordFactory(wf);
        cs.setFilterFactory(new DiffuseRippleFilterFactory());
    }

    public Patchca length(int lenth) {
        wf.setMaxLength(lenth);
        wf.setMinLength(lenth);
        cs.setWordFactory(wf);
        return this;
    }

    public Patchca length(int min, int max) {
        wf.setMinLength(min);
        wf.setMaxLength(max);
        cs.setWordFactory(wf);
        return this;
    }

    public Patchca size(int width, int height) {
        cs.setWidth(width);
        cs.setHeight(height);
        return this;
    }

    public Patchca color(ColorFactory colorFactory) {
        cs.setColorFactory(colorFactory);
        return this;
    }

    public Patchca word(WordFactory wordFactory) {
        cs.setWordFactory(wordFactory);
        return this;
    }

    public Patchca filter(FilterFactory filterFactory) {
        cs.setFilterFactory(filterFactory);
        return this;
    }

    @Override
    public String render() throws PatchcaException {
        return this.render(DEFAULT_SESSION_KEY);
    }

    @Override
    public String render(String sessionKey) throws PatchcaException {
        try {
            Session session = WebContext.request().session();
            Request request = WebContext.request();

            ChannelHandlerContext ctx = WebContext.get().getChannelHandlerContext();

            File file = File.createTempFile("blade_code_", ".png");
            FileOutputStream fos = new FileOutputStream(file);
            String token = EncoderHelper.getChallangeAndWriteImage(cs, "png", fos);
            session.attribute(sessionKey, token);

            log.debug("current sessionid = [{}], token = [{}]", session.id(), token);

            DefaultHttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            setResponseHeaders(WebContext.response());

            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(file, "r");
                long fileLength = raf.length();

                httpResponse.headers().set(NettyHttpConst.CONTENT_LENGTH, fileLength);
                if (request.keepAlive()) {
                    httpResponse.headers().set(NettyHttpConst.CONNECTION, NettyHttpConst.KEEP_ALIVE);
                }

                // Write the initial line and the header.
                ctx.write(httpResponse);

                // Write the content.
                ChannelFuture sendFileFuture;
                ChannelFuture lastContentFuture;
                if (ctx.pipeline().get(SslHandler.class) == null) {
                    sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
                    // Write the end marker.
                    lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

                } else {
                    sendFileFuture = ctx.writeAndFlush(
                            new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                            ctx.newProgressivePromise());
                    // HttpChunkedInput will write the end marker (LastHttpContent) for us.
                    lastContentFuture = sendFileFuture;
                }

                sendFileFuture.addListener(ProgressiveFutureListener.build(raf));

                // Decide whether to close the connection or not.
                if (!request.keepAlive()) {
                    lastContentFuture.addListener(ChannelFutureListener.CLOSE);
                }
            } catch (FileNotFoundException e) {
                throw new PatchcaException(e);
            }
            return token;
        } catch (IOException e) {
            throw new PatchcaException(e);
        }
    }

    private void setResponseHeaders(Response response) {
        response.contentType("image/png");
        response.header("Cache-Control", "no-cache, no-store");
        response.header("Pragma", "no-cache");
        long time = System.currentTimeMillis();
        response.header("Last-Modified", time + "");
        response.header("Date", time + "");
        response.header("Expires", time + "");
    }

    @Override
    public boolean verify(String code) {
        return this.verify(code, DEFAULT_SESSION_KEY);
    }

    @Override
    public boolean verify(String code, String sessionKey) {
        Session session = WebContext.request().session();
        String sessionCode = session.attribute(sessionKey);
        return code.equals(sessionCode);
    }

    @Override
    public File create(String imgPath, String imgType) throws PatchcaException {
        try {
            FileOutputStream fos = new FileOutputStream(imgPath);
            EncoderHelper.getChallangeAndWriteImage(cs, imgType, fos);
            fos.close();
            return new File(imgPath);
        } catch (IOException e) {
            throw new PatchcaException(e);
        }
    }

}
