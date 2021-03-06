package com.example.shiranpeer.burritolocator.presenter;

import com.example.shiranpeer.burritolocator.repository.MainActivityRepository;
import com.example.shiranpeer.burritolocator.screen.mainActivity.MainActivityMvpView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivityPresenter {
    private MainActivityRepository repository;
    private Disposable subscription;
    private MainActivityMvpView mvpView;
    private final static String OK = "OK";
    private final static int MAX_TRIES = 4;

    public MainActivityPresenter(MainActivityRepository repository, MainActivityMvpView mvpView) {
        this.mvpView = mvpView;
        this.repository = repository;
    }

    public void requestNearbyPlaces(String types, String locations, String keyword, String rankBy, String apiKey) {
        if (!mvpView.isNetworkAvailable()) {
            mvpView.showNetworkErrorMessage();
        } else {
            subscription = repository.getNearbyPlaces(types, locations, keyword, rankBy, apiKey)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(placesResult -> {
                                if (placesResult.getStatus().equals(OK)) {
                                    mvpView.hideProgressBar();
                                    mvpView.showNearbyPlaces(placesResult.getPlaces(),
                                            placesResult.getNextPageToken());
                                } else {
                                    //TODO: error message should be according to status (based on requirements)
                                    mvpView.showGPSErrorMessage();
                                }
                            },
                            e -> {
                                mvpView.hideProgressBar();
                                mvpView.showGPSErrorMessage();
                            });
        }
    }

    public void requestNearbyPlacesAdditionalResults(String pageToken, String apiKey) {
        if (!mvpView.isNetworkAvailable()) {
            mvpView.hideProgressBarLoader();
            mvpView.showNetworkErrorMessageForLoading();
        } else {
            subscription = repository.getNearbyPlacesAdditionalResults(pageToken, apiKey)
                    .repeat()
                    .takeUntil(placesResult -> {
                        int retry = placesResult.getRequestRetry();
                        retry++;
                        placesResult.setRequestRetry(retry);
                        return placesResult.getStatus().equals(OK) || retry == MAX_TRIES;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(placesResult -> {
                                mvpView.showNearbyAdditionalPlaces(placesResult.getPlaces(),
                                        placesResult.getNextPageToken());
                            },
                            e -> {
                                mvpView.showNetworkErrorMessageForLoading();
                                mvpView.hideProgressBarLoader();
                            });
        }
    }
}
