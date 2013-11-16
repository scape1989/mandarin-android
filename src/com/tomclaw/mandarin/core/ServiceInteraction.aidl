package com.tomclaw.mandarin.core;

import com.tomclaw.mandarin.core.CoreObject;

interface ServiceInteraction
{
    // Staff methods
	int getServiceState();
	long getUpTime();
    String getAppSession();
	// Accounts API
	List getAccountsList();
	void addAccount(in CoreObject accountRoot);
	boolean removeAccount(int accountDbId);
	void updateAccountStatus(String accountType, String userId, int statusIndex);
}