package com.memoize.api.Config.Security;

import java.util.UUID;

public record AuthPrincipal (
    UUID id,
    String role
) {}
