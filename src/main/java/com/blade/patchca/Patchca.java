package com.blade.patchca;

import org.patchca.color.ColorFactory;
import org.patchca.filter.FilterFactory;
import org.patchca.word.WordFactory;

import java.io.File;

public interface Patchca {

    String render() throws PatchcaException;

    String render(String sessionKey) throws PatchcaException;

    boolean verify(String code);

    boolean verify(String code, String sessionKey);

    File create(String imgPath, String imgType) throws PatchcaException;

    Patchca length(int lenth);

    Patchca length(int min, int max);

    Patchca size(int width, int height);

    Patchca color(ColorFactory colorFactory);

    Patchca word(WordFactory wordFactory);

    Patchca filter(FilterFactory filterFactory);

}
