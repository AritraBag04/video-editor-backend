package com.liquidator.messages;

import com.liquidator.input_service.Input;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresignedURLMessage {
    private String requestid;
    private Input input;
}
