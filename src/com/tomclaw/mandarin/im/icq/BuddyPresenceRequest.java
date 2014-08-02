package com.tomclaw.mandarin.im.icq;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.tomclaw.mandarin.core.CoreService;
import com.tomclaw.mandarin.core.RequestHelper;
import com.tomclaw.mandarin.core.Settings;
import com.tomclaw.mandarin.im.Gender;
import com.tomclaw.mandarin.im.ShortBuddyInfo;
import com.tomclaw.mandarin.im.StatusNotFoundException;
import com.tomclaw.mandarin.im.StatusUtil;
import com.tomclaw.mandarin.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tomclaw.mandarin.im.icq.WimConstants.RESPONSE_OBJECT;
import static com.tomclaw.mandarin.im.icq.WimConstants.STATUS_CODE;

/**
 * Created by Solkin on 24.07.2014.
 */
public class BuddyPresenceRequest extends WimRequest {

    private int total;
    private int skipped;
    private Map<String, ShortBuddyInfo> shortInfoMap;
    private IcqSearchOptionsBuilder searchOptions;

    public BuddyPresenceRequest() {
    }

    public BuddyPresenceRequest(int total, int skipped, Map<String, ShortBuddyInfo> shortInfoMap,
                                IcqSearchOptionsBuilder searchOptions) {
        this.total = total;
        this.skipped = skipped;
        this.shortInfoMap = shortInfoMap;
        this.searchOptions = searchOptions;
    }

    @Override
    protected int parseJson(JSONObject response) throws JSONException {
        Intent intent;
        // Start to JSON parsing.
        JSONObject responseObject = response.getJSONObject(RESPONSE_OBJECT);
        int statusCode = responseObject.getInt(STATUS_CODE);
        // Check for server reply.
        if (statusCode == WIM_OK) {
            JSONObject data = responseObject.getJSONObject("data");
            JSONArray users = data.getJSONArray("users");
            for (int i = 0; i < users.length(); i++) {
                JSONObject buddy = users.getJSONObject(i);
                JSONObject profile = buddy.optJSONObject("profile");
                ShortBuddyInfo buddyInfo = shortInfoMap.get(buddy.getString("aimId"));
                if (profile != null && buddyInfo != null) {
                    String state = buddy.getString("state");
                    String buddyIcon = buddy.optString("buddyIcon", null);
                    int statusIndex;
                    try {
                        statusIndex = StatusUtil.getStatusIndex(getAccountRoot().getAccountType(), state);
                    } catch (StatusNotFoundException ignored) {
                        // Wow, this is a pretty strange status!
                        statusIndex = StatusUtil.STATUS_OFFLINE;
                    }
                    buddyInfo.setOnline(statusIndex != StatusUtil.STATUS_OFFLINE);
                    // Create custom avatar request.
                    if (!TextUtils.isEmpty(buddyIcon)) {
                        Log.d(Settings.LOG_TAG, "search avatar for " + buddyInfo.getBuddyId() + ": " + buddyIcon);
                        RequestHelper.requestSearchAvatar(getAccountRoot().getContentResolver(),
                                getAccountRoot().getAccountDbId(), buddyInfo.getBuddyId(),
                                CoreService.getAppSession(), buddyIcon);
                    }
                    // Parsing profile.
                    buddyInfo.setBuddyNick(StringUtil.fixCyrillicSymbols(profile.optString("friendlyName")));
                    buddyInfo.setFirstName(StringUtil.fixCyrillicSymbols(profile.optString("firstName")));
                    buddyInfo.setLastName(StringUtil.fixCyrillicSymbols(profile.optString("lastName")));
                    String gender = profile.optString("gender");
                    if (!TextUtils.equals(gender, "unknown")) {
                        buddyInfo.setGender(gender.equals("male") ? Gender.Male : Gender.Female);
                    }
                    JSONArray homeAddress = profile.optJSONArray("homeAddress");
                    if (homeAddress != null) {
                        String city = "";
                        for (int c = 0; c < homeAddress.length(); c++) {
                            if (c > 0) {
                                city += ", ";
                            }
                            city += StringUtil.fixCyrillicSymbols(homeAddress.getJSONObject(c).optString("city"));
                        }
                        if (!TextUtils.isEmpty(city)) {
                            buddyInfo.setHomeAddress(city);
                        }
                    }
                    long birthDate = profile.optLong("birthDate") * 1000;
                    if (birthDate > 0) {
                        buddyInfo.setBirthDate(birthDate);
                    }
                }
            }
            intent = BuddySearchRequest.getSearchResultIntent(getAccountRoot().getAccountDbId(),
                    searchOptions, total, skipped, shortInfoMap);
        } else {
            intent = BuddySearchRequest.getNoResultIntent(getAccountRoot().getAccountDbId(), searchOptions);
        }
        // We must send intent in any case,
        // because our request is going to be deleted.
        getService().sendBroadcast(intent);
        return REQUEST_DELETE;
    }

    @Override
    protected String getUrl() {
        return getAccountRoot().getWellKnownUrls().getWebApiBase()
                .concat("presence/get");
    }

    @Override
    protected List<Pair<String, String>> getParams() {
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("aimsid", getAccountRoot().getAimSid()));
        params.add(new Pair<String, String>("f", WimConstants.FORMAT_JSON));
        params.add(new Pair<String, String>("mdir", "1"));
        for (String buddyId : shortInfoMap.keySet()) {
            params.add(new Pair<String, String>("t", buddyId));
        }
        return params;
    }
}
