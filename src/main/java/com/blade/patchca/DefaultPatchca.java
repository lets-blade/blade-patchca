package com.blade.patchca;

import com.blade.kit.StringKit;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;
import com.blade.mvc.http.wrapper.Session;
import org.patchca.color.ColorFactory;
import org.patchca.filter.FilterFactory;
import org.patchca.filter.predefined.DiffuseRippleFilterFactory;
import org.patchca.service.ConfigurableCaptchaService;
import org.patchca.utils.encoder.EncoderHelper;
import org.patchca.word.RandomWordFactory;
import org.patchca.word.WordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.Random;

/**
 * DefaultPatchca
 */
public class DefaultPatchca implements Patchca {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPatchca.class);

	private ConfigurableCaptchaService cs = null;
	private static Random random = new Random();
	
	private RandomWordFactory wf;
	
	public DefaultPatchca() {
		this(new ColorFactory() {
			@Override
			public Color getColor(int x) {
				int[] c = new int[3];
				int i = random.nextInt(c.length);
				for (int fi = 0; fi < c.length; fi++) {
					if (fi == i) {
						c[fi] = random.nextInt(71);
					} else {
						c[fi] = random.nextInt(256);
					}
				}
				return new Color(c[0], c[1], c[2]);
			}
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
	
	public Patchca length(int lenth){
		wf.setMaxLength(lenth);
		wf.setMinLength(lenth);
		cs.setWordFactory(wf);
		return this;
	}
	
	public Patchca length(int min, int max){
		wf.setMinLength(min);
		wf.setMaxLength(max);
		cs.setWordFactory(wf);
		return this;
	}
	
	public Patchca size(int width, int height){
		cs.setWidth(width);  
	    cs.setHeight(height);  
		return this;
	}
	
	public Patchca color(ColorFactory colorFactory){
		cs.setColorFactory(colorFactory);
		return this;
	}
	
	public Patchca word(WordFactory wordFactory){
		cs.setWordFactory(wordFactory);
		return this;
	}
	
	public Patchca filter(FilterFactory filterFactory){
		cs.setFilterFactory(filterFactory);
		return this;
	}
	
	public void render(Request request, Response response) throws PatchcaException{
		render(request, response, "patchca");
	}
	
	public void render(Request request, Response response, String patchca) throws PatchcaException{
		try {
			Session session = request.session();
			setResponseHeaders(response);
			OutputStream out = response.outputStream();
			String token = EncoderHelper.getChallangeAndWriteImage(cs, "png", out);
			session.attribute(patchca, token);
			out.flush();
			out.close();
			LOGGER.debug("current sessionid = [{}], token = [{}]", session.id(), token);
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
	
	public boolean validation(String patchca, Response response){
		return validation(patchca, "png", response);
	}
	
	public boolean validation(String patchca, String imgType, Response response){
		try {
			String token = EncoderHelper.getChallangeAndWriteImage(cs, imgType, response.outputStream());
			if(StringKit.isBlank(patchca)){
				return false;
			}
			return token.equalsIgnoreCase(patchca);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String token(String imgType, Response response) throws PatchcaException{
		try {
			return EncoderHelper.getChallangeAndWriteImage(cs, imgType, response.outputStream());
		} catch (IOException e) {
			throw new PatchcaException(e);
		}
	}
	
	public String token(Response response) throws PatchcaException{
		try {
			String token = EncoderHelper.getChallangeAndWriteImage(cs, "png", response.outputStream());
			return token;
		} catch (IOException e) {
			throw new PatchcaException(e);
		}
	}
	
	public File create(String imgPath, String imgType) throws PatchcaException {
		try {
			FileOutputStream fos = new FileOutputStream(imgPath);
			EncoderHelper.getChallangeAndWriteImage(cs, imgType, fos);
			fos.close();
			return new File(imgPath);
		} catch (FileNotFoundException e) {
			throw new PatchcaException(e);
		} catch (IOException e) {
			throw new PatchcaException(e);
		}
	}
}
