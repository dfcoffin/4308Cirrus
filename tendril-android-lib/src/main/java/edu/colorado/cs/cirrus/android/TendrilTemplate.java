package edu.colorado.cs.cirrus.android;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import edu.colorado.cs.cirrus.domain.intf.ITendril;
import edu.colorado.cs.cirrus.domain.model.AccessGrant;
import edu.colorado.cs.cirrus.domain.model.CostAndConsumption;
import edu.colorado.cs.cirrus.domain.model.DeviceData;
import edu.colorado.cs.cirrus.domain.model.Device;
import edu.colorado.cs.cirrus.domain.model.Devices;
import edu.colorado.cs.cirrus.domain.model.ExternalAccountId;
import edu.colorado.cs.cirrus.domain.model.MeterReading;
import edu.colorado.cs.cirrus.domain.model.PricingProgram;
import edu.colorado.cs.cirrus.domain.model.PricingSchedule;
import edu.colorado.cs.cirrus.domain.model.SetThermostatDataRequest;
import edu.colorado.cs.cirrus.domain.model.User;
import edu.colorado.cs.cirrus.domain.model.UserProfile;

public class TendrilTemplate implements ITendril {

	private static final String BASE_URL = "https://dev.tendrilinc.com/connect/";
	private static final String ACCESS_TOKEN_URL = "https://dev.tendrilinc.com/oauth/access_token";
	private static final String APP_KEY = "925272ee5d12eac858aeb81949671584";
	private static final String APP_SECRET = "3230f8f0aa064bea145d425c57fe8679";
	private static final String SCOPE = "offline_access";
	private static final String USERNAME = "csci4138@tendrilinc.com";
	private static final String PASSWORD = "password";
	private static final String THERMOSTAT_CATEGORY = "Thermostat";

	private static final String GET_USER_INFO_URL = BASE_URL
			+ "user/current-user";
	private static final String GET_USER_PROFILE_URL = BASE_URL
			+ "user/current-user/profile";
	private static final String GET_USER_LOCATION_PROFILE_URL = BASE_URL
			+ "user/current-user/account/default-acccount/location/default-location/profile/household";
	private static final String GET_USER_EXTERNAL_ACCOUNT_ID_URL = BASE_URL
			+ "user/current-user/account/default-account";
	private static final String GET_METER_READINGS_URL = BASE_URL
			+ "meter/read;external-account-id={external-account-id};from={from};to={to};limit-to-latest={limit-to-latest};source={source}";
	private static final String GET_HISTORICAL_COST_AND_CONSUMPTION_URL = BASE_URL
			+ "user/current-user/account/default-account/consumption/{resolution};from={from};to={to};limit-to-latest={limit-to-latest}";
	private static final String GET_PROJECTED_COST_AND_CONSUMPTION_URL = BASE_URL
			+ "user/current-user/account/default-account/consumption/{resolution}/projection;source={source}";
	private static final String GET_PRICING_PROGRAM_URL = BASE_URL
			+ "user/current-user/account/default-account/pricing/current-pricing-program";
	private static final String GET_PRICING_SCHEDULE_URL = BASE_URL
			+ "pricing/schedule;external-account-id={external-account-id}";
	private static final String GET_DEVICE_LIST_URL = BASE_URL
			+ "user/current-user/account/default-account/location/default-location/network/default-network/device;include-extended-properties=true";
	private static final String POST_DEVICE_ACTION_URL = BASE_URL
			+ "device-action";
	private static final String GET_COST_AND_CONSUMPTION_MY_NEIGHBORS_URL = BASE_URL
			+ "user/current-user/account/default-account/comparison/myneighbors/{resolution};from={from};to={to}";
	private static final String GET_COST_AND_CONSUMPTION_BASELINEACTUAL_URL = BASE_URL
			+ "user/current-user/account/default-account/comparison/baselineactual/{resolution};asof={asof}";

	private HttpEntity<?> requestEntity;
	private HttpHeaders requestHeaders;
	private RestTemplate restTemplate;
	// private String accessToken = "uninitialized";
	// private String refreshToken = "uninitialized";
	private long expiresIn = 0l;
	private AccessGrant accessGrant = null;
	// private static final String TAG = "TendrilTemplate";
	private User user = null;
	private UserProfile userProfile = null;
	private ExternalAccountId externalAccountId = null;
	private Devices devices = null;
	private Device tstat = null;

	/**
	 * Create a new instance of TendrilTemplate. This constructor creates the
	 * TendrilTemplate using a username and password.
	 * 
	 * 
	 * @param login
	 * @param password
	 **/
	public TendrilTemplate(String login, String password) {
		System.err.println("Initializing TendrilTemplate1");

		this.restTemplate = new RestTemplate();
		// The HttpComponentsClientHttpRequestFactory uses the
		// org.apache.http package to make network requests
		restTemplate
				.setRequestFactory(new HttpComponentsClientHttpRequestFactory(
						HttpUtils.getNewHttpClient()));

		logIn();

		List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
		acceptableMediaTypes.add(MediaType.APPLICATION_XML);
		requestHeaders = new HttpHeaders();
		requestHeaders.setAccept(acceptableMediaTypes);
		requestHeaders.setContentType(MediaType.APPLICATION_XML);
		requestHeaders.set("access_token", this.accessGrant.getAccess_token());
		requestEntity = new HttpEntity<Object>(requestHeaders);
	}

	private boolean authorize(boolean refresh) {

		DateTime expiration = new DateTime();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.set("Accept", "application/json");
		params.set("Content-Type", "application/x-www-form-urlencoded");

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("scope", SCOPE);

		if (refresh) {
			formData.add("grant_type", "refresh_token");
			formData.add("refresh_token", accessGrant.getRefresh_token());

		} else {
			formData.add("client_id", APP_KEY);
			formData.add("client_secret", APP_SECRET);
			formData.add("grant_type", "password");
			formData.add("username", getUsername());
			formData.add("password", getPassword());
		}
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		requestHeaders.set("Accept", "application/json");
		// Populate the MultiValueMap being serialized and headers in an
		// HttpEntity object to use for the request
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(
				formData, requestHeaders);

		ResponseEntity<AccessGrant> response = restTemplate.exchange(
				ACCESS_TOKEN_URL, HttpMethod.POST, requestEntity,
				AccessGrant.class);

		System.err.println(response);
		accessGrant = response.getBody();
		accessGrant.setExpirationDateTime(expiration
				.plusSeconds((int) accessGrant.getExpires_in()));

		return true;
	}

	private CostAndConsumption fetchCostAndConsumption(Resolution resolution,
			DateTime from, DateTime to, Integer limitToLatest) {
		String fromString = from.toString(ISODateTimeFormat.dateTimeNoMillis());
		String toString = to.toString(ISODateTimeFormat.dateTimeNoMillis());
		System.err.println(fromString);

		Object[] vars = { resolution, fromString, toString, limitToLatest };

		ResponseEntity<CostAndConsumption> costAndConsumption = restTemplate
				.exchange(GET_HISTORICAL_COST_AND_CONSUMPTION_URL,
						HttpMethod.GET, requestEntity,
						CostAndConsumption.class, vars);
		System.err.println(costAndConsumption.getBody());
		return costAndConsumption.getBody();

	}

	public CostAndConsumption fetchCostAndConsumptionRange(DateTime from,
			DateTime to) {

		// String toString = to.toString(ISODateTimeFormat.dateTimeNoMillis());
		// System.err.println(fromString);
		return fetchCostAndConsumption(Resolution.RANGE, from, to, 1);

	}

	public Devices fetchDevices() {
		ResponseEntity<Devices> devices = restTemplate.exchange(
				GET_DEVICE_LIST_URL, HttpMethod.GET, requestEntity,
				Devices.class);

		System.err.println(devices.getBody());
		return devices.getBody();
	}

	public ExternalAccountId fetchExternalAccountId() {

		ResponseEntity<ExternalAccountId> response = restTemplate.exchange(
				GET_USER_EXTERNAL_ACCOUNT_ID_URL, HttpMethod.GET,
				requestEntity, ExternalAccountId.class);
		System.err.println(response.getBody());
		this.externalAccountId = response.getBody();
		return externalAccountId;
	}

	private MeterReading fetchMeterReading(DateTime from, DateTime to,
			Integer limitToLatest, Source source) {

		String fromString = from.toString(ISODateTimeFormat.dateTimeNoMillis());
		String toString = to.toString(ISODateTimeFormat.dateTimeNoMillis());

		Object[] vars = { getUser().getId(), toString, fromString,
				limitToLatest, source };

		ResponseEntity<MeterReading> meterReading = restTemplate.exchange(
				GET_METER_READINGS_URL, HttpMethod.GET, requestEntity,
				MeterReading.class, vars);
		System.err.println(meterReading.getBody());
		return meterReading.getBody();

	}

	public MeterReading fetchMeterReadingRange(DateTime from, DateTime to) {
		return fetchMeterReading(from, to, 100, Source.ACTUAL);
	}

	public PricingProgram fetchPricingProgram() {
		// Object[] vars = { getExternalAccountId().getId() };
		ResponseEntity<PricingProgram> pricingSchedule = restTemplate.exchange(
				GET_PRICING_PROGRAM_URL, HttpMethod.GET, requestEntity,
				PricingProgram.class);
		return pricingSchedule.getBody();

	}

	// FIXME: this does not currently work- API documentation is inconsistent
	public PricingSchedule fetchPricingSchedule(DateTime from, DateTime to) {
		// String fromString =
		// from.toString(ISODateTimeFormat.dateTimeNoMillis());
		// String toString = to.toString(ISODateTimeFormat.dateTimeNoMillis());
		// System.err.println(fromString);

		Object[] vars = { getExternalAccountId() }; //IS this the right param?
		ResponseEntity<PricingSchedule> profile = restTemplate.exchange(
				GET_PRICING_SCHEDULE_URL, HttpMethod.GET, requestEntity,
				PricingSchedule.class, vars);
		return profile.getBody();
	}

	public User fetchUser() {
		ResponseEntity<User> response = restTemplate.exchange(
				GET_USER_INFO_URL, HttpMethod.GET, requestEntity, User.class);
		System.err.println(response.getBody());
		user = response.getBody();
		return user;
	}

	//FIXME: throws a 404 even for current-user. See Tendril's "try it" page
	public UserProfile fetchUserProfile(){
		ResponseEntity<UserProfile> response = restTemplate.exchange(
				GET_USER_PROFILE_URL, HttpMethod.GET, requestEntity, UserProfile.class);
		System.err.println(response.getBody());
		userProfile = response.getBody();
		return userProfile;
	}

	public Devices getDevices() {
		if (devices == null)
			devices = fetchDevices();
		return devices;
	}
	
	public String getExternalAccountId() {
		if (this.externalAccountId == null)
		    fetchExternalAccountId();
		return externalAccountId.getExternalAccountId();
	}
	
	private String getLocationId() {
		if (user == null)
			fetchUser();
		return user.getId();
	}

	private String getPassword() {
		return PASSWORD;
	}

	public Device getTstat() {
		if (tstat == null) {
			for (Device d : getDevices().getDevice()) {
				if (d.getCategory().equalsIgnoreCase(THERMOSTAT_CATEGORY)) {
					tstat = d;
					System.err.println("tstat: " + tstat);
					break;
				}
			}
		}
		return tstat;
	}

	public User getUser() {
		if (user == null)
			fetchUser();
		return user;
	}

	private String getUsername() {
		return USERNAME;
	}

	public UserProfile getUserProfile(){
		if(this.userProfile == null){
			this.userProfile = fetchUserProfile();
		}return this.userProfile;
	}

	// TODO: now that we are getting 2 year tokens, maybe we should just
	// wait for an error connecting before checking expiration time
	public boolean isConnected() {
		DateTime now = new DateTime();
		if (accessGrant != null && accessGrant.getExpirationDateTime() != null) {
			if (now.isBefore(accessGrant.getExpirationDateTime()
					.minusMinutes(5))) {
				System.err
						.println("isConnected(): Valid access token! expires: "
								+ accessGrant.getExpirationDateTime());
				return true;
			} else {
				return refreshToken();
			}
		} else {
			return logIn();
		}
	}

	public boolean logIn() {
		return authorize(false);
	}

	public boolean logOut() {
		// TODO: actually log out of Tendril server
		accessGrant = null;
		return true;

	}

	private boolean refreshToken() {
		return authorize(true);
	}

	private void setRequestEntity() {

		// Populate the headers in an HttpEntity object to use for the
		// request

		// public RestOperations restOperations() {
		// return getRestTemplate();
		// }

		// protected void configureRestTemplate(RestTemplate restTemplate) {
		// restTemplate.setErrorHandler(new TendrilErrorHandler());
		// }

	}

	public SetThermostatDataRequest setTstatSetpoint(Float setpoint) {
	    restTemplate = new RestTemplate();
	    List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new SimpleXmlHttpMessageConverter());
	    restTemplate.setMessageConverters(messageConverters);
	    
		SetThermostatDataRequest stdr = new SetThermostatDataRequest();
		stdr.setDeviceId(getTstat().getDeviceId());
		stdr.setLocationId(getLocationId());
		stdr.setRequestId("none");
		DeviceData data = new DeviceData();
		data.setMode("Heat");
		data.setSetpoint(setpoint.toString());
		data.setTemperatureScale("Fahrenheit");

		stdr.setData(data);
		System.err.println("Request: " + stdr);

		List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
		acceptableMediaTypes.add(MediaType.APPLICATION_XML);

		requestHeaders = new HttpHeaders();
		requestHeaders.setAccept(acceptableMediaTypes);
		requestHeaders.setContentType(MediaType.APPLICATION_XML);
		requestHeaders.set("access_token", this.accessGrant.getAccess_token());
		// requestEntity = new
		// HttpEntity<SetThermostatDataRequest>(requestHeaders);

		/*
		 * String request =
		 * "<setThermostatDataRequest deviceId=\"001db700000246f5\" locationId=\"74\" requestId=\"none\" xmlns=\"http://platform.tendrilinc.com/tnop/extension/ems\"><data><setpoint>77.0</setpoint><mode>Heat</mode>"
		 * +
		 * "<temperatureScale>Fahrenheit</temperatureScale></data></setThermostatDataRequest>"
		 * ;
		 */

		HttpEntity<SetThermostatDataRequest> requestEntity = new HttpEntity<SetThermostatDataRequest>(
				stdr, requestHeaders);

		SetThermostatDataRequest stdrResponse = null;
		try {
			// List<HttpMessageConverter<?>> mcList = ;

			for (HttpMessageConverter<?> hmc : restTemplate
					.getMessageConverters()) {
				if (hmc.canWrite(SetThermostatDataRequest.class,
						MediaType.APPLICATION_XML)) {
					System.err.println("canWrite: " + hmc.getClass());
					// HttpOutputMessage outputMessage = new
					// CommonsClientHttpRequestFactory().createRequest(uri,
					// httpMethod);
					// hmc.write(stdr, MediaType.APPLICATION_XML, outputMessage
					// )
				}
				if (hmc.canRead(SetThermostatDataRequest.class,
						MediaType.APPLICATION_XML)) {
					System.err.println("canRead: " + hmc.getClass());

				}

			}

			ResponseEntity<String> response = restTemplate.exchange(
					POST_DEVICE_ACTION_URL, HttpMethod.POST, requestEntity,
					String.class);

			// stdrResponse = response.getBody();
			System.err.println(response.getBody());
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			System.err.println(e.getResponseBodyAsString());
		}
		return stdrResponse;
	}

}
