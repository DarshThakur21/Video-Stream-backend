package com.stream.app.VideoStream.Payload;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomMessage {
    private String message;
    private boolean success=false;
}
