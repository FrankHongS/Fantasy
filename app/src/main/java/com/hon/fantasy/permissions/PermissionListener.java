package com.hon.fantasy.permissions;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public interface PermissionListener {
    /**
     * Gets called each time we run Nammu.permissionCompare() and some Permission is revoke/granted to us
     *
     * @param permissionChanged
     */
    void permissionsChanged(String permissionChanged);

    /**
     * Gets called each time we run Nammu.permissionCompare() and some Permission is granted
     *
     * @param permissionGranted
     */
    void permissionsGranted(String permissionGranted);

    /**
     * Gets called each time we run Nammu.permissionCompare() and some Permission is removed
     *
     * @param permissionRemoved
     */
    void permissionsRemoved(String permissionRemoved);
}
