package com.messaging.user.dto;

import java.util.List;

public record UserPage(List<UserSummary> users, boolean hasNext) {}
