### Deprecated dependencies

- Replaced all occurrences of springdoc for springfox (Open API)
- Updated to spring boot 3.2.1
- Updated to spring cloud 2023.0.0 (removed all deprecated APIs)
- Replaced spring cloud stream with functional style. 
- Introduced StreamBridge in place of ```MessageChannel````interface.

 ### Changes in Code

 - Replaced all existing WebSecurityConfigurer Adapters.
 - Removed all occurrences of ```javax.*``` with ```jakarta.*``` as required by boot3.
 
 ### Structural changes
 - Added docker using ```jib-maven ``` build
 - Removed auth-server in favour of spring authorization server
 
### To Consider
- Checking the method to replace globalResponseMessage(POST, emptyList())
- Cleaning up the store services OpenAPI config.

### Dockerization
- Used jib-maven for now (spotify dependency repository is archived)
- Can be refactored to use either ``` buildPacks``` or ``` Graalvm ```

### Testing
- Use of spring cloud stream binder tests
- Concentrated on use of binder and bindings for unit and integration tests
- Emphasis on lambda based spring cloud stream functional implementations
