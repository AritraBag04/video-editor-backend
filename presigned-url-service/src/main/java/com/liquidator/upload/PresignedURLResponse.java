package com.liquidator.upload;

import java.util.List;
import java.util.UUID;

public record PresignedURLResponse(
        UUID projectId,
        List<String> preSignedURLs
) {}