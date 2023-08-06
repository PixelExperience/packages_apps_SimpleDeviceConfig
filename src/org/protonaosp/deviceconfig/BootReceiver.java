/*
 * Copyright (C) 2020 The Proton AOSP Project
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

package org.protonaosp.deviceconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.Log;
import android.os.SystemProperties;
import java.util.Arrays;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "SimpleDeviceConfig";

    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            Log.i(TAG, "Updating device config at boot");
            updateDefaultConfigs(context);
        }).start();
    }

    private void updateDefaultConfigs(Context context) {
        resetConfigs(context, R.array.reset_namespaces);
        updateConfig(context, R.array.configs_base, false);
        updateConfig(context, R.array.configs_base_soft, true);

        updateConfig(context, R.array.configs_device, false);

        boolean isPixelDevice = Arrays.asList(context.getResources().getStringArray(R.array.pixel_devices))
            .contains(SystemProperties.get("org.pixelexperience.device"));
        if (isPixelDevice){
            updateConfig(context, R.array.configs_base_pixel, false);
        }
    }

    private void updateConfig(Context context, int configArray, boolean isSoft) {
        // Set current properties
        String[] rawProperties = context.getResources().getStringArray(configArray);
        for (String property : rawProperties) {
            // Format: namespace/key=value
            String[] kv = property.split("=");
            String fullKey = kv[0];
            String[] nsKey = fullKey.split("/");

            String namespace = nsKey[0];
            String key = nsKey[1];
            String value = "";
            if (kv.length > 1) {
                value = kv[1];
            }

            // Skip soft configs that already have values
            if (!isSoft || DeviceConfig.getString(namespace, key, null) == null) {
                DeviceConfig.setProperty(namespace, key, value, false);
            }
        }
    }

    private void resetConfigs(Context context, int configArray) {
        // Reset namespace configs
        String[] namespaces = context.getResources().getStringArray(configArray);
        for (String namespace : namespaces) {
            DeviceConfig.resetToDefaults(Settings.RESET_MODE_TRUSTED_DEFAULTS, namespace);
        }
    }
}
