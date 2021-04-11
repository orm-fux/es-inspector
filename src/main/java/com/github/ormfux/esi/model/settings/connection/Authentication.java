package com.github.ormfux.esi.model.settings.connection;

public interface Authentication {
	
	String getAuthenticationHeaderValue();
	
	String getType();
}
