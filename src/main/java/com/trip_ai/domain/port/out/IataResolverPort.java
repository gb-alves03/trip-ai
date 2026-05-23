package com.trip_ai.domain.port.out;

public interface IataResolverPort {
    String resolve(String cityOrCode);
}
