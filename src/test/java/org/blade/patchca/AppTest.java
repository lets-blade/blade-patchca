package org.blade.patchca;

import java.io.IOException;

import com.hellokaton.blade.patchca.DefaultPatchca;
import com.hellokaton.blade.patchca.Patchca;
import com.hellokaton.blade.patchca.PatchcaException;

public class AppTest {

    public static void main(String[] args) throws IOException, PatchcaException {
        Patchca patchca = new DefaultPatchca();

        // 生成一个验证码到本地
        patchca.create("F:/aaa.png", "png");
    }

}
