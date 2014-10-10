package com.pac.performance.fragments;

import com.pac.performance.R;
import com.pac.performance.utils.Constants;
import com.pac.performance.utils.Dialog.DialogReturn;

import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class KernelSamepageMerging extends PreferenceFragment implements
        Constants {

    private final Handler hand = new Handler();

    private Preference[] mInfos;

    private CheckBoxPreference mEnableKsm;
    private Preference mPagesToScan, mSleepMilliseconds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceScreen root = getPreferenceManager()
                .createPreferenceScreen(getActivity());
        setPreferenceScreen(root);

        new Thread() {
            public void run() {
                root.addPreference(prefHelper.setPreferenceCategory(
                        getString(R.string.ksm_stats), getActivity()));

                mInfos = new Preference[KSM_INFOS.length];
                for (int i = 0; i < KSM_INFOS.length; i++) {
                    mInfos[i] = prefHelper.setPreference(getResources()
                            .getStringArray(R.array.ksm_infos)[i], String
                            .valueOf(kernelsamepagemergingHelper.getInfos(i)),
                            getActivity());
                    root.addPreference(mInfos[i]);
                }

                root.addPreference(prefHelper.setPreferenceCategory(
                        getString(R.string.parameters), getActivity()));

                mEnableKsm = prefHelper.setCheckBoxPreference(
                        kernelsamepagemergingHelper.isKsmActive(),
                        getString(R.string.ksm_enable),
                        getString(R.string.ksm_enable_summary), getActivity());
                root.addPreference(mEnableKsm);

                mPagesToScan = prefHelper.setPreference(
                        getString(R.string.ksm_pages_to_scan), String
                                .valueOf(kernelsamepagemergingHelper
                                        .getPagesToScan()), getActivity());
                root.addPreference(mPagesToScan);

                mSleepMilliseconds = prefHelper.setPreference(
                        getString(R.string.ksm_sleep_milliseconds),
                        kernelsamepagemergingHelper.getSleepMilliseconds()
                                + getString(R.string.ms), getActivity());
                root.addPreference(mSleepMilliseconds);
            }
        }.start();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            final Preference preference) {

        if (preference == mEnableKsm) mCommandControl.runGeneric(KSM_RUN,
                mEnableKsm.isChecked() ? "1" : "0", -1, getActivity());

        if (preference == mPagesToScan) {
            String[] values = new String[1025];
            for (int i = 0; i < values.length; i++)
                values[i] = String.valueOf(i);

            mDialog.showSeekBarDialog(values, values, preference.getSummary()
                    .toString(), new DialogReturn() {
                @Override
                public void dialogReturn(String value) {
                    mCommandControl.runGeneric(KSM_PAGES_TO_SCAN, value, -1,
                            getActivity());
                    preference.setSummary(value + getString(R.string.value));
                }
            }, getActivity());
        }

        if (preference == mSleepMilliseconds) {
            String[] modifiedvalues = new String[5001];
            for (int i = 0; i < modifiedvalues.length; i++)
                modifiedvalues[i] = i + getString(R.string.ms);

            String[] values = new String[5001];
            for (int i = 0; i < values.length; i++)
                values[i] = String.valueOf(i);

            mDialog.showSeekBarDialog(modifiedvalues, values, preference
                    .getSummary().toString(), new DialogReturn() {
                @Override
                public void dialogReturn(String value) {
                    mCommandControl.runGeneric(KSM_SLEEP_MILLISECONDS, value,
                            -1, getActivity());
                    preference.setSummary(value);
                }
            }, getActivity());
        }

        return true;
    }

    @Override
    public void onResume() {
        hand.post(run);
        super.onResume();
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < KSM_INFOS.length; i++)
                        mInfos[i].setSummary(String
                                .valueOf(kernelsamepagemergingHelper
                                        .getInfos(i)));

                    hand.postDelayed(run, 1000);
                }
            });

        }
    };

    @Override
    public void onDestroy() {
        hand.removeCallbacks(run);
        super.onDestroy();
    };
}
