/* Logger interfaces for messages generated */

package com.plantandfood.aspirelite;

interface Logger {
    /* Log some messages or errors */
    void clear();
    void message(String message);
    void error(String error);

}
