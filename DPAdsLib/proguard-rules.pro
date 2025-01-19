-keep class android.webkit.** { *; }
-dontwarn android.webkit.**
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe

-keep class com.facebook.infer.annotation.** { *; }
-dontwarn com.facebook.infer.annotation.**

-keep public class com.mbridge.* extends androidx.** { *; }
-keep public class androidx.viewpager.widget.PagerAdapter{*;}
#-keep public class androidx.viewpager.widget.ViewPager.OnPageChangeListener{*;}
-keep interface androidx.annotation.IntDef{*;}
-keep interface androidx.annotation.Nullable{*;}
-keep interface androidx.annotation.CheckResult{*;}
-keep interface androidx.annotation.NonNull{*;}
-keep public class androidx.fragment.app.Fragment{*;}
-keep public class androidx.core.content.FileProvider{*;}
-keep public class androidx.core.app.NotificationCompat{*;}
-keep public class androidx.appcompat.widget.AppCompatImageView {*;}
-keep public class androidx.recyclerview.*{*;}
-keep class com.mbridge.msdk.foundation.tools.FastKV{*;}
-keep class com.mbridge.msdk.foundation.tools.FastKV$Builder{*;}

-keep class com.dp.ads.lib.activities.AppCompatBaseActivity.** { *; }
-keep class com.dp.ads.lib.activities.LanguageScreenOne.** { *; }
-keep class com.dp.ads.lib.activities.LanguageScreenDup.** { *; }
-keep class com.dp.ads.lib.activities.WalkThroughConfigActivity.** { *; }
-keep class com.dp.ads.lib.activities.WelcomeScreenOne.** { *; }
-keep class com.dp.ads.lib.activities.WelcomeScreenDup.** { *; }
-keep class com.dp.ads.lib.activities.WTFullScreenAdFragment.** { *; }
-keep class com.dp.ads.lib.activities.WTOneFragment.** { *; }
-keep class com.dp.ads.lib.activities.WTThreeFragment.** { *; }
-keep class com.dp.ads.lib.activities.WTTwoFragment.** { *; }

-keep class com.dp.ads.lib.adapters.LanguageAdapter.** { *; }
-keep class com.dp.ads.lib.adapters.WalkThroughAdapter.** { *; }

-keep class com.dp.ads.lib.adMobAdClasses.AdMobBannerAdSplash.** { *; }
-keep class com.dp.ads.lib.adMobAdClasses.AdmobInterstitialAdSplash.** { *; }
-keep class com.dp.ads.lib.adMobAdClasses.AdmobNativeAdManager.** { *; }
-keep class com.dp.ads.lib.adMobAdClasses.AdmobResumeAdSplash.** { *; }

-keep class com.dp.ads.lib.callingClasses.LanguageScreensConfiguration.** { *; }
-keep class com.dp.ads.lib.callingClasses.DPAdsConfigurations.** { *; }
-keep class com.dp.ads.lib.callingClasses.DPAdsManager.** { *; }
-keep class com.dp.ads.lib.callingClasses.WalkThroughScreensConfiguration.** { *; }
-keep class com.dp.ads.lib.callingClasses.WelcomeScreensConfiguration.** { *; }

-keep class com.dp.ads.lib.data.Language.** { *; }
-keep class com.dp.ads.lib.data.WalkThroughItem.** { *; }

-keep interface com.dp.ads.lib.interfaces.LanguageInterface.** { *; }
-keep interface com.dp.ads.lib.interfaces.WelcomeInterface.** { *; }
-keep interface com.dp.ads.lib.interfaces.OnNextButtonClickListener.** { *; }

-keep class com.dp.ads.lib.metaAdClasses.MetaBannerAdSplash.** { *; }
-keep class com.dp.ads.lib.metaAdClasses.MetaInterstitialAdSplash.** { *; }
-keep class com.dp.ads.lib.metaAdClasses.MetaNativeAdManager.** { *; }

-keep class com.dp.ads.lib.utils.AdLoadingDialog.** { *; }
-keep class com.dp.ads.lib.utils.ExtesntionFunctionsKt.** { *; }
-keep class com.dp.ads.lib.utils.MyLocaleHelper.** { *; }
-keep class com.dp.ads.lib.utils.NetworkCheck.** { *; }
-keep class com.dp.ads.lib.utils.PrefHelper.** { *; }

-keep class com.dp.ads.lib.utilsGoogleAdsConsent.ConsentConfigurations.** { *; }
-keep class com.dp.ads.lib.utilsGoogleAdsConsent.GoogleMobileAdsConsentManager.** { *; }