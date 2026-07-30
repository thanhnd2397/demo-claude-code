package vn.thanhnd.demo.domain.adapter;

import vn.thanhnd.demo.domain.model.Administrator;

/**
 * Port for notifying an administrator about a successful login.
 */
public interface LoginNotificationPort {

    /**
     * Notify an administrator that a successful login just occurred. Implementations run
     * asynchronously and must not propagate failures back to the caller.
     *
     * @param administrator The administrator who just logged in
     */
    void notifyLogin(Administrator administrator);
}
