package com.kabutar.gatekeeper.filter;

public class PreGatewayFilterOrder {
	public static final int HIGHEST_ORDER = Integer.MIN_VALUE;
	public static final int RATE_LIMITED_FILTER_ORDER = HIGHEST_ORDER + 100;
	public static final int URI_REPLACE_FILTER_ORDER = HIGHEST_ORDER + 1000;
	public static final int PROTECTED_ROUTE_FILTER_ORDER = HIGHEST_ORDER + 2000;
}
