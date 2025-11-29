package com.artpond.backend.config;

import com.artpond.backend.map.domain.Place;
import com.artpond.backend.map.dto.PlaceDataDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);

        modelMapper.addMappings(new PropertyMap<Place, PlaceDataDto>() {
            @Override
            protected void configure() {
                map().setLatitude(source.getCoordinates().getY());
                map().setLongitude(source.getCoordinates().getX());
            }
        });

        return modelMapper;
    }
}