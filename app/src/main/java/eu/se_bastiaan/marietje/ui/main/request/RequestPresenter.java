package eu.se_bastiaan.marietje.ui.main.request;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import eu.se_bastiaan.marietje.data.DataManager;
import eu.se_bastiaan.marietje.data.model.Empty;
import eu.se_bastiaan.marietje.data.model.Songs;
import eu.se_bastiaan.marietje.events.NeedsCsrfToken;
import eu.se_bastiaan.marietje.injection.ApplicationContext;
import eu.se_bastiaan.marietje.injection.PerFragment;
import eu.se_bastiaan.marietje.ui.base.BasePresenter;
import eu.se_bastiaan.marietje.util.EventBus;
import eu.se_bastiaan.marietje.util.RxSubscriber;
import eu.se_bastiaan.marietje.util.RxUtil;
import eu.se_bastiaan.marietje.util.TextUtil;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

@PerFragment
public class RequestPresenter extends BasePresenter<RequestView> {

    private final DataManager dataManager;
    private final Context context;
    private final EventBus eventBus;

    private String currentQuery;
    private int currentPage = 0;
    private Subscription searchSubscription;

    @Inject
    public RequestPresenter(DataManager dataManager, @ApplicationContext Context context, EventBus eventBus) {
        this.dataManager = dataManager;
        this.context = context;
        this.eventBus = eventBus;
    }

    @Override
    public void attachView(@NonNull RequestView mvpView) {
        super.attachView(mvpView);
    }

    public void searchSong(@NonNull String queryStr) {
        if (queryStr.length() > 0 && queryStr.length() < 3) {
            return;
        }

        if (TextUtil.equals(queryStr, currentQuery)) {
            currentPage++;
        } else {
            currentPage = 1;
        }

        currentQuery = queryStr;
        doSearch(queryStr);
    }

    private void doSearch(String queryStr) {
        checkViewAttached();

        RxUtil.unsubscribe(searchSubscription);

        if (currentPage == 1) {
            view().showLoading();
        }

        searchSubscription = dataManager.songsDataManager().songs(currentPage, queryStr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<Songs>() {
                    @Override
                    public void onNext(Songs response) {
                        if (response.getData().isEmpty()) {
                            view().showSongsEmpty();
                        } else {
                            view().showSongs(response.getData(), response.getCurrentPage() == 1, response.getCurrentPage() != response.getLastPage());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e);
                        view().showLoadingError();

                        if (e instanceof HttpException) {
                            HttpException httpException = (HttpException) e;
                            if (httpException.code() == 403) {
                                eventBus.post(new NeedsCsrfToken());
                            }
                        }
                    }
                });

        unsubscribeOnDetachView(searchSubscription);
    }

    public void requestSong(long songId) {
        checkViewAttached();

        Subscription subscription = dataManager.controlDataManager().request(songId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<Empty>() {
                    @Override
                    public void onNext(Empty emptyResponse) {
                        view().showRequestSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e);
                        view().showRequestError();
                    }
                });

        unsubscribeOnDetachView(subscription);
    }

}
