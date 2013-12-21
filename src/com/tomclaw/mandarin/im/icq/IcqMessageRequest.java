package com.tomclaw.mandarin.im.icq;

import android.util.Pair;
import com.tomclaw.mandarin.core.QueryHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.tomclaw.mandarin.im.icq.WimConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 6/12/13
 * Time: 1:38 PM
 */
public class IcqMessageRequest extends WimRequest {

    private String to;
    private String message;
    private String cookie;

    public IcqMessageRequest() {
    }

    public IcqMessageRequest(String to, String message, String cookie) {
        this.to = to;
        this.message = message;
        this.cookie = cookie;
    }

    @Override
    protected int parseJson(JSONObject response) throws JSONException {
        JSONObject responseObject = response.getJSONObject(RESPONSE_OBJECT);
        int statusCode = responseObject.getInt(STATUS_CODE);
        // Check for server reply.
        if (statusCode == WIM_OK) {
            String requestId = responseObject.getString(REQUEST_ID);
            JSONObject dataObject = responseObject.getJSONObject(DATA_OBJECT);
            String state = dataObject.getString(STATE);
            // Checking for message state.
            for (int i = 0; i < IM_STATES.length; i++) {
                if (state.equals(IM_STATES[i])) {
                    QueryHelper.updateMessageState(getAccountRoot().getContentResolver(), requestId, i);
                    break;
                }
            }
            return REQUEST_DELETE;
        } else if (statusCode == 462 || statusCode >= 600 && statusCode < 700) { // TODO: check this status codes.
            // Target error.
            String requestId = responseObject.getString(REQUEST_ID);
            QueryHelper.updateMessageState(getAccountRoot().getContentResolver(), requestId, 1);
            return REQUEST_DELETE;
        }
        // Maybe incorrect aim sid or McDonald's.
        return REQUEST_PENDING;
    }

    @Override
    protected String getUrl() {
        return getAccountRoot().getWellKnownUrls().getWebApiBase()
                .concat("im/sendIM");
    }

    @Override
    protected List<Pair<String, String>> getParams() {
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("aimsid", getAccountRoot().getAimSid()));
        params.add(new Pair<String, String>("autoResponse", "false"));
        params.add(new Pair<String, String>("f", "json"));
        params.add(new Pair<String, String>("message", message));
        params.add(new Pair<String, String>("notifyDelivery", "true"));
        params.add(new Pair<String, String>("offlineIM", "true"));
        params.add(new Pair<String, String>("r", cookie));
        params.add(new Pair<String, String>("t", to));
        return params;
    }
}
