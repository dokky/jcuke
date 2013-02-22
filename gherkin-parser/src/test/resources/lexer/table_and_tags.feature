Using step definitions from: '../steps'

@aps @security
Feature: APS Security
	As an MSDP admin
	When I set up user accounts
	I want to be able to restrict them by roles and IP address ranges
	
	Background:
		Given APS application
		And users:
			| user_name | password	| unit_id	| roles													|
			| opsadmin	| pwd		| null		| ACCOUNT_PROVISIONING_SERVICE_ADMIN,OPERATIONS_ADMIN 	|
	
	@5949
	Scenario Outline: IP Mask Validation does not generate the right error message (MSDP-5949)
		Given providers:
			| unit_id	|
			| 42		|
		And users:
			| user_name | password	| unit_id	| roles													|
			| ps42		| pwd		| 42		| ACCOUNT_PROVISIONING_SERVICE_ADMIN,PROVIDER_ADMIN		|
		And I expect the content to contain '<message>'
		
		Examples:
			| user_name	| parameters											| header			| header_value					| http_code		| message													| 
			| ps42		| subscriberId=test1&credentials=pwd1&disablePaa=true	| ORIG_IP			| 243.432.16.11					| 401			| Invalid Ip Address 243.432.16.11 for user "ps42"			| 
			| ps42		| subscriberId=test1&credentials=pwd1&disablePaa=true	| X-Forwarded-For	| 243.432.16.13					| 401			| *			|

			
	@5960
	Scenario Outline: APS REST API denies access to global users with non-global privileges (MSDP-5960)
		Given providers:
			| unit_id |
			| 2029    |
			| 2030    |
		And users:
			| user_name | password	| unit_id	|
			| global	| pwd		| null		|
		And I expect the content to contain '<message>'
		
		Examples: name
			| user_name	| unit_id	| parameters											| http_code		| message													| 
			| global	| 2029		| subscriberId=test1&credentials=pwd1&disablePaa=true	| 201			| /softwareProviderUnit/2029/account/test1					| 
