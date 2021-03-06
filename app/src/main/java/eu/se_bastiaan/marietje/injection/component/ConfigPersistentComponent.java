package eu.se_bastiaan.marietje.injection.component;

import dagger.Component;
import eu.se_bastiaan.marietje.injection.ConfigPersistent;
import eu.se_bastiaan.marietje.injection.module.ActivityModule;
import eu.se_bastiaan.marietje.ui.base.BaseActivity;

/**
 * A dagger component that will live during the lifecycle of an Activity but it won't
 * be destroy during configuration changes. Check {@link BaseActivity} to see how this components
 * survives configuration changes.
 * Use the {@link ConfigPersistent} scope to annotate dependencies that need to survive
 * configuration changes (for example Presenters).
 */
@ConfigPersistent
@Component(dependencies = AppComponent.class)
public interface ConfigPersistentComponent {

    ActivityComponent activityComponent(ActivityModule activityModule);

}