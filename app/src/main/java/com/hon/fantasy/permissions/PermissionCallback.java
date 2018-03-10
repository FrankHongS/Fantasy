package com.hon.fantasy.permissions;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public interface PermissionCallback {
    void permissionGranted();

    void permissionRefused();
}
