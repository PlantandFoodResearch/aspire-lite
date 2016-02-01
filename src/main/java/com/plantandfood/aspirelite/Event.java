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
