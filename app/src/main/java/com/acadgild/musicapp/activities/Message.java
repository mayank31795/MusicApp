package com.acadgild.musicapp.activities;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by MayankGoyal on 27-Jul-16.
 */
@JsonObject
public class Message {

    /*
     * Annotate a field that you want sent with the @JsonField marker.
     */
    @JsonField
    public String description;
    public String player;
    /*
     * Note that since this field isn't annotated as a
     * @JsonField, LoganSquare will ignore it when parsing
     * and serializing this class.
     */
    public int nonJsonField;
}
