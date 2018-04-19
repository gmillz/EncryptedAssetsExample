/*
 * Copyright (C) 2017 SlimRoms Project
 * Copyright (C) 2017 Victor Lapin
 * Copyright (C) 2017 Griffin Millender
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slimroms.themecore;

import android.content.res.AssetManager;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

public class AssetUtils {

    private static final String TAG = "AssetUtils";

    public static boolean copyAssetFolder(AssetManager am,
                                          String assetPath, String path) {
        return copyAssetFolder(am, assetPath, path, null);
    }

    public static boolean copyAssetFolder(AssetManager am,
                                          String assetPath, String path, Cipher cipher) {
        try {
            String[] files = am.list(assetPath);
            if (!new File(path).exists() && !new File(path).mkdirs()) {
                throw new RuntimeException("cannot create directory: " + path);
            }
            boolean res = true;
            for (String file : files) {
                if (am.list(assetPath + "/" + file).length == 0) {
                    res &= copyAsset(am, assetPath + "/" + file, path + "/" + file, cipher);
                } else {
                    res &= copyAssetFolder(am, assetPath + "/" + file, path + "/" + file, cipher);
                }
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyAsset(AssetManager assetManager,
                                    String fromAssetPath, String toPath) {
        return copyAsset(assetManager, fromAssetPath, toPath, null);
    }

    public static boolean copyAsset(AssetManager assetManager,
                                    String fromAssetPath, String toPath, Cipher cipher) {
        InputStream in;
        File parent = new File(toPath).getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            Log.d(TAG, "Unable to create " + parent.getAbsolutePath());
        }

        try {
            in = assetManager.open(fromAssetPath);
            if (cipher != null && fromAssetPath.endsWith(".enc")) {
                in = new CipherInputStream(in, cipher);
                if (toPath.endsWith(".enc")) {
                    toPath = toPath.substring(0, toPath.lastIndexOf("."));
                }
            }
            String text = IOUtils.toString(in, Charset.defaultCharset());
            Log.d("TEST", "text=" + text);
            copyInputStreamToFile(in, new File(toPath));
            in.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}