package com.blade.patchca;

import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;

import java.io.File;

public interface Patchca {

    void render(Request request, Response response) throws PatchcaException;

    void render(Request request, Response response, String patchca) throws PatchcaException;

    boolean validation(String patchca, Response response);

    boolean validation(String patchca, String imgType, Response response);

    String token(String imgType, Response response) throws PatchcaException;

    String token(Response response) throws PatchcaException;

    File create(String imgPath, String imgType) throws PatchcaException;
}
