package com.pesit.excavator;

/*
 EXCAVATOR JAVA LIBRARY
 Java client wrapper which talks to excavator Python API
 -> provides a object representation of JSON data
 -> provides few methods to ease the process of data extraction
 -> provides filters to impose SQL kind of where clause
 -> provides asyc database access with callbacks 	
 */
 
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;


public class DataApi {

	public static String APIkey ="users";
	public static String serverIP ="192.168.180.146";	
	//second parameter for filter method
	public static final int LESSTHAN =2;
	public static final int GREATERTHAN =1;
	public static final int EQUALS =0;

	//trigger co-oresponding flask methods
	private String path ="";
	
	//store parameters
	//parameter name
	private List<String> pvalue = new ArrayList();
	private List<String> pname = new ArrayList();
	private int limit=-1;
	
	//number of filters
	private static int fcount=0;

	//similar to select * from table
	public DataApi getAll()
	{
		path="/getAll";
		return this;
	}
	
	//retrives last n rows from the table
	public DataApi getLastN(int n)
	{
		path="/getLastN";
		pvalue.add(String.valueOf(n));
		pname.add("getLastN");
		return this;
	}

	//fileters can be used to specify constraints similar to where clause in SQL 
	public DataApi filter(String attribute,int operation,int key)
	{
		pvalue.add(String.valueOf(attribute));
		pname.add("filteratt"+fcount);
		
		pvalue.add(String.valueOf(operation));
		pname.add("filterop"+fcount);
		
		pvalue.add(String.valueOf(key));
		pname.add("filterkey"+fcount);
		
		fcount+=1;
		
		return this;
	}
	
	//similar to limit constraint of SQL
	public DataApi limit(int n)
	{
		limit=n;
		return this;
	}
	
	//exceute a SQL query
	public DataApi execute(String s)
	{
		path="/execute";
		pvalue.add(s);
		pname.add("execute");
		return this;
	}

	//creates a http asyc GET request to interact with the python API
	public void run(final Object o, final Response r)
	{
		////
		//make the asyc call
		////
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(serverIP).setPort(5001).setPath(path)
	    .setParameter("APIkey", DataApi.APIkey)
	    .setParameter("filterCount", String.valueOf(fcount))
	    .setParameter("limit", String.valueOf(limit));
	    
		for(int i=0;i<pvalue.size();i++)
			builder.setParameter(pname.get(i), pvalue.get(i));
	
		URI requestURL = null;
		try {
			requestURL = builder.build();
			System.out.println(requestURL);
		}
		catch (URISyntaxException use) {
			System.out.println("SET APIKEY AND CHECK FORMAT OF IP \ncatch in URI builder : "+use );
		}
		
		ExecutorService threadpool = Executors.newFixedThreadPool(2);
		Async async = Async.newInstance().use(threadpool);
		final org.apache.http.client.fluent.Request request = org.apache.http.client.fluent.Request.Get(requestURL);
		
		Future<Content> future = async.execute(request, new FutureCallback<Content>() {
			
		public void failed (final Exception e) {
	        //System.out.println("FAILED : "+e.getMessage() +": "+ request);
	        r.onFailure(e.getMessage() +": "+ request);
	    }
	    
	    public void completed (final Content content) {
	    	//call to createOject will convert json data to objects
	    	r.onSuccess(createObject(o, content.asString()));
	    }

	    
	    public void cancelled () 
	    {
	    	
	    }
	    
	   
		});
		
		// clear of the flags and data sored in list for previous request
		clearContext();
	}
	
	//Clear context after each http request
	private void clearContext()
	{
		path ="";
		
		pvalue = new ArrayList();
		pname = new ArrayList();
		
		
		fcount=0;
		limit=-1;
	}
	
	//convert JSON to java objects 
	private Object[] createObject(Object o, String json_str)
	{	

		//Use Gson api to extract content from JSON string
		int start = json_str.indexOf('[');
		
		int end = json_str.indexOf(']');
		String newstr = json_str.substring(start,end+1);
	 	Gson gson = new Gson();
	 	//System.out.println("JSON string is" + newstr);
	 	newstr.replaceAll("\"", "\'");
		ArrayList<LinkedTreeMap<String,String>> la = new ArrayList<LinkedTreeMap<String,String>>();
		List<LinkedTreeMap<String,String>> list = gson.fromJson(newstr, la.getClass()); //populating list of lists with json data
		
		//Create an array of generic Object type
		
		Object[] array_return = new Object[list.size()]; 
		Class aclass = o.getClass();	//we get the class type from object passed.
		
		int i = 0;
		for(LinkedTreeMap<String,String> a:list)
		{
			
			
			Object o1 = null;
			try 
			{
				o1 = aclass.newInstance();
			}
			catch (InstantiationException e2) 
			{
				// TODO Auto-generated catch block
				System.out.println("ERROR Instantiating user class ");
				e2.printStackTrace();
			}
			catch (IllegalAccessException e2) 
			{
				// TODO Auto-generated catch block
				System.out.println("ERROR Instantiating user class ");
				e2.printStackTrace();
			}
			//traverse the ds to get key,value pairs for each row.
			for(Map.Entry<String, String> m: a.entrySet())
			{
				String key = m.getKey();
				String val = m.getValue();
				

				try 
				{
					//gets the field whose name is 'key' and stores it in field
					Field field = aclass.getField(key);
					//sets the 'field' of object o1 as val
					field.set(o1,val); 
					//System.out.println("Setting value "+val+" for variable "+key);
					
				}
				catch (NoSuchFieldException e) 
				{
					// TODO Auto-generated catch block
					System.out.println("CATCH due to class JSON mismatch");
					e.printStackTrace();
					
				} 
				catch (SecurityException e) 
				{
					// TODO Auto-generated catch block
					System.out.println("CATCH due to class JSON mismatch");
					e.printStackTrace();
				}
				catch (IllegalArgumentException e1) 
				{
					// TODO Auto-generated catch block
					System.out.println("CATCH due to class JSON mismatch");
					e1.printStackTrace();
				}
				catch (IllegalAccessException e1) 
				{
					// TODO Auto-generated catch block
					System.out.println("CATCH due to class JSON mismatch");
					e1.printStackTrace();
				}
				
			}
			
			//assign the object to one element of the array_return
			array_return[i++] = o1;
			
		}
	
		//System.out.println("Finally!!" + array_return);
		return array_return;
		//return o1;
	}
	
}

