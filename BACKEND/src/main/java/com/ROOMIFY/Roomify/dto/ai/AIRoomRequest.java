package com.ROOMIFY.Roomify.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class AIRoomRequest {
    private String sourceImageUrl;
    private List<String> furniture;
    private String style;
    private Double roomWidth;
    private Double roomLength;
    private Double ceilingHeight;
    private String additionalInstructions;
}
