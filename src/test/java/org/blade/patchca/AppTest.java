package org.blade.patchca;

import java.io.IOException;

import com.blade.patchca.DefaultPatchca;
import com.blade.patchca.Patchca;

public class AppTest {
	
	public static void main(String[] args) throws IOException {
		
		Patchca patchca = new DefaultPatchca();
		// 生成一个验证码到本地
		patchca.create("F:/aaa.png", "png");
	}
}
