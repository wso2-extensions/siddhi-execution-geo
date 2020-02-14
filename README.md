Siddhi-execution-geo
======================================

The **siddhi-execution-geo extension** is an extension to <a target="_blank" href="https://wso2.github.io/siddhi">Siddhi</a> that provides geo data related functionality such as checking whether a given geo coordinate is within a predefined geo-fence, etc. Following are the functions of the Geo extension.

Find some useful links below:

* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-geo">Source code</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-geo/releases">Releases</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-geo/issues">Issue tracker</a>

## Latest API Docs 

Latest API Docs is <a target="_blank" href="https://wso2-extensions.github.io/siddhi-execution-geo/api/5.0.1">5.0.1</a>.

## How to use 

**Using the extension in <a target="_blank" href="https://github.com/wso2/product-sp">WSO2 Stream Processor</a>**

* You can use this extension in the latest <a target="_blank" href="https://github.com/wso2/product-sp/releases">WSO2 Stream Processor</a> that is a part of <a target="_blank" href="http://wso2.com/analytics?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">WSO2 Analytics</a> offering, with editor, debugger and simulation support. 

* This extension is shipped by default with WSO2 Stream Processor, if you wish to use an alternative version of this 
extension you can replace the component <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-geo/releases">jar</a> that can be found in the `<STREAM_PROCESSOR_HOME>/lib` 
directory.

**Using the extension as a <a target="_blank" href="https://wso2.github.io/siddhi/documentation/running-as-a-java-library">java library</a>**

* This extension can be added as a maven dependency along with other Siddhi dependencies to your project.

```
     <dependency>
        <groupId>org.wso2.extension.siddhi.execution.geo</groupId>
        <artifactId>siddhi-execution-geo</artifactId>
        <version>x.x.x</version>
     </dependency>
```

## Jenkins Build Status


---

|  Branch | Build Status |
| :------ |:------------ | 
| master  | [![Build Status](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-execution-geo/badge/icon)](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-execution-geo/) |

--- 



## Features

* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-execution-geo/api/5.0.1/#findcityfromip-function">findCityFromIP</a> *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#function">(Function)</a>*<br><div style="padding-left: 1em;"><p>This function returns the city that is related to the given IP address.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-execution-geo/api/5.0.1/#findcountryfromip-function">findCountryFromIP</a> *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#function">(Function)</a>*<br><div style="padding-left: 1em;"><p>This function returns the country that is related to the given IP address.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-execution-geo/api/5.0.1/#geocode-stream-function">geocode</a> *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#stream-function">(Stream Function)</a>*<br><div style="padding-left: 1em;"><p>The geo code stream function uses basic details relating to a location (e.g., street name, number,etc.) as the input and returns the longitude, latitude, and the address of that location. </p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-execution-geo/api/5.0.1/#geocoordinate-stream-function">geocoordinate</a> *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#stream-function">(Stream Function)</a>*<br><div style="padding-left: 1em;"><p>The geocoordinate stream function returns the longitude and latitude values of a location relating to a given IPV4 or IPV6 address.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-execution-geo/api/5.0.1/#reversegeocode-stream-function">reversegeocode</a> *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#stream-function">(Stream Function)</a>*<br><div style="padding-left: 1em;"><p>This extension transforms pairs of latitude and longitude coordinates into precise address information. The output contains string properties including the 'streetNumber', 'neighborhood', 'route', 'administrativeAreaLevelTwo', 'administrativeAreaLevelOne', 'country', 'countryCode', 'postalCode', and the 'formattedAddress' in the given order. However, this information is not available for all the geo coordinates. For example, if the latitude and longitude represent a place in a forest, only the high level information such as the country is returned. In such scenarios, "N/A" is returned as the value for return attributes of which the values cannot be derived.</p></div>

## Dependencies

In order to use the functions supported by this extension, import the latest geo location data to the `LOCATION` and `BLOCKS` tables as follows:

1. First, create an account in the [Maxmind site](https://dev.maxmind.com/geoip/geoip2/geolite2/).

2. Then log in to the Maxmind site using your newly created credentials via the [Login Form](https://www.maxmind.com/en/accounts/200820/geoip/downloads).

3. Download the `geoip-2-csv-converter` that is compatible with your operating system from the [maxmind/geoip2-csv-converter github repository](https://github.com/maxmind/geoip2-csv-converter/releases).

4. Download and install a database type of your choice. Then create the required database and tables as follows:

       !!!info
           This example uses MySQL.

      1. Create a database named `GEO_LOCATION_DATA`.

      2. To create the required tables in the database, execute one of the following scripts.

          - [db2.sql](../resources/db2.sql)
          - [mssql.sql](../resources/mssql.sql)
          - [mysql.sql](../resources/mysql.sql)
          - [oracle.sql](../resources/oracle.sql)
          - [postgresql.sql](../resources/postgresql.sql)

         It creates two tables named `BLOCKS` and `LOCATION`.

           !!! info
               - In this example, `mysql.sql` database script is executed.
               - To execute the database script, you can use [MySQL Workbench](https://dev.mysql.com/downloads/workbench/). For detailed instructions to run the database script, see [MySQL Documentation - The Workbench Scripting Shell](https://dev.mysql.com/doc/workbench/en/wb-scripting-shell.html).

      3. Download a JDBC provider depending on the database you are using (MySQL, in this example), and copy it to the `<SI_HOME>/lib` directory.

      4. Configure the datasource for the Geo location in the `<SI_HOME>/conf/server/deployment.yaml` file as follows.

            ```
                - name: GEO_LOCATION_DATA
                  description: "The data source used for geo location database"
                  jndiConfig:
                    name: jdbc/GEO_LOCATION_DATA
                  definition:
                    type: RDBMS
                    configuration:
                      jdbcUrl: 'jdbc:h2:${sys:carbon.home}/wso2/worker/database/GEO_LOCATION_DATA;AUTO_SERVER=TRUE'
                      username: wso2carbon
                      password: wso2carbon
                      driverClassName: org.h2.Driver
                      maxPoolSize: 50
                      idleTimeout: 60000
                      validationTimeout: 30000
                      isAutoCommit: false

            ```


5. Prepare the database entries as follows:

    1. Unzip the latest CSV file and the `geoip-2-csv-converter` that you downloaded in the steps above.

    2. Run the [update-geolocation-data.sh](https://docs.wso2.com/download/attachments/97564367/update-geolocation-data.sh?version=2&modificationDate=1580908358000&api=v2) file by issuing the following command:

        `sh update-geolocation-data.sh`

    3. Enter the path to the extracted `GeoLite2-City-Blocks-IPv4` directory that you downloaded as the response for the `Enter path to GeoLite2-City-Blocks-IPv4 directory:` prompt (e.g., `/<PATH_TO>/GeoLite2-City-CSV_20171107`).

    4. Enter the path to the `geoip2-csv-converter` directory as the response for the `Enter path to geoip2-csv-converter home directory:` prompt (e.g., `/<PATH_TO>/geoip2-csv-converter-v1.0.0`).

  Once the script is executed, you can find the `final.csv` file inside your current directory.


6. Import the data as follows:

    1. To import the `final.csv` file (which you previously generated) into the `BLOCKS` table, issue the following command after logging into the MySQL console.

        ```
        load data local infile '[PATH_TO_FINAL.CSV]/final.csv' into table BLOCKS
         fields terminated by ','
         enclosed by '"'
         lines terminated by '\n'
         (network_cidr, network, broadcast, geoname_id, registered_country_geoname_id, represented_country_geoname_id, is_anonymous_proxy, is_satellite_provider, postal_code, latitude, longitude, network_blocks);
        ```

    2. To import the `GeoLite2-City-Locations-en.csv` file located inside the extracted `geoip-2-csv-converter` directory (e.g., `geoip-2-csv-converterGeoLite2-City-CSV_2017110`) into the `LOCATION` table, issue the following command.

        ```
        load data local infile '[PATH_TO_GeoLite2-City-Locations-en]/GeoLite2-City-Locations-en.csv' into table LOCATION
         fields terminated by ','
         enclosed by '"'
         lines terminated by '\n'
         (geoname_id, locale_code, continent_code, continent_name, country_iso_code, country_name, subdivision_1_iso_code, subdivision_1_name, subdivision_2_iso_code, subdivision_2_name, city_name, metro_code, time_zone);
        ```

7. Restart the Streaming Integrator. You have now updated the Geo Location Data Set.



## How to Contribute
 
  * Please report issues at <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-geo/issues">GitHub Issue Tracker</a>.
  
  * Send your contributions as pull requests to <a target="_blank" href="https://github.com/wso2-extensions/siddhi-execution-geo/tree/master">master branch</a>. 
 
## Contact us 

 * Post your questions with the <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">"Siddhi"</a> tag in <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">Stackoverflow</a>. 
 
 * Siddhi developers can be contacted via the mailing lists:
 
    Developers List   : [dev@wso2.org](mailto:dev@wso2.org)
    
    Architecture List : [architecture@wso2.org](mailto:architecture@wso2.org)
 
## Support 

* We are committed to ensuring support for this extension in production. Our unique approach ensures that all support leverages our open development methodology and is provided by the very same engineers who build the technology. 

* For more details and to take advantage of this unique opportunity contact us via <a target="_blank" href="http://wso2.com/support?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">http://wso2.com/support/</a>. 


