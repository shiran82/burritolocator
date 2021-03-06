package com.example.shiranpeer.burritolocator.repository;

import com.example.shiranpeer.burritolocator.model.PlacesResult;

import io.reactivex.Observable;


public interface MainActivityRepository {
    Observable<PlacesResult> getNearbyPlaces(String restaurant, String s, String burrito, String distance, String googlePlaceApiKey);

    Observable<PlacesResult> getNearbyPlacesAdditionalResults(String pageToken, String googlePlaceApiKey);
}
