package com.pesit.excavator;

public interface Response {
	
	public void onSuccess(Object[] result);
	public void onFailure(String error);

}
