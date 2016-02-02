/* Wrapper class containing a level and a message, used internally in Log for holding
 * level/message pairs.
 */

package com.plantandfood.aspirelite;

class Event {
    /* Wrapper class containing a specific event */

    int level;
    CharSequence message;

    public Event(int newLevel, CharSequence newMessage) {
        level = newLevel;
        message = newMessage;
    }
}
