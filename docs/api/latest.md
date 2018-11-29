# API Docs - v4.0.17-SNAPSHOT

## Geo

### findCityFromIP *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#function">(Function)</a>*

<p style="word-wrap: break-word">Returns the city which is related to the give ip address.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
<STRING> geo:findCityFromIP(<STRING> ip)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">ip</td>
        <td style="vertical-align: top; word-wrap: break-word">The IP address that the user need to get the relevant city</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
define stream IpStream(ip string);
from IpStream
select geo:getCity(ip) as city
insert into outputStream;
```
<p style="word-wrap: break-word">This will return the corresponding city to the given ip address</p>

### findCountryFromIP *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#function">(Function)</a>*

<p style="word-wrap: break-word">Returns the country which is related to the give ip address</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
<STRING> geo:findCountryFromIP(<STRING> ip)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">ip</td>
        <td style="vertical-align: top; word-wrap: break-word">The IP address that the user need to get the relevant country</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
define stream IpStream(ip string);
from IpStream
select geo:getCountry(ip) as country 
insert into outputStream;
```
<p style="word-wrap: break-word">This will return the corresponding country to the given ip address</p>

### geocode *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-function">(Stream Function)</a>*

<p style="word-wrap: break-word">Geo code stream function</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
geo:geocode(<STRING> location)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">location</td>
        <td style="vertical-align: top; word-wrap: break-word">location details(Street name, number etc.)</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>
<span id="extra-return-attributes" class="md-typeset" style="display: block; font-weight: bold;">Extra Return Attributes</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Possible Types</th>
    </tr>
    <tr>
        <td style="vertical-align: top">longitude</td>
        <td style="vertical-align: top; word-wrap: break-word">Longitude of the location</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
    <tr>
        <td style="vertical-align: top">latitude</td>
        <td style="vertical-align: top; word-wrap: break-word">Latitude of the location</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
    <tr>
        <td style="vertical-align: top">address</td>
        <td style="vertical-align: top; word-wrap: break-word">Location details</td>
        <td style="vertical-align: top">STRING</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
geocode("5 Avenue Anatole France, 75007 Paris, France")
```
<p style="word-wrap: break-word">This will returns the longitude and latitude of the given location with the location details. so the results are 48.8588871d, 2.2944861d, "5 Avenue Anatole France, 75007 Paris, France"</p>

### geocoordinate *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-function">(Stream Function)</a>*

<p style="word-wrap: break-word">geocoordinate stream function returns the longitude and latitude values of a location which is related to the given IPV4 or IPV6 address.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
geo:geocoordinate(<STRING> ip)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">ip</td>
        <td style="vertical-align: top; word-wrap: break-word">The IP address that the user need to get the longitude and latitude</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">apiurl</td>
        <td style="vertical-align: top; word-wrap: break-word">ipInfoDB(https://www.ipinfodb.com/) provides an API toget IP information from their IP address geolocation database.This API provides an url to get the information based on IP address</td>
        <td style="vertical-align: top">N/A</td>
        <td style="vertical-align: top">N/A</td>
    </tr>
</table>
<span id="extra-return-attributes" class="md-typeset" style="display: block; font-weight: bold;">Extra Return Attributes</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Possible Types</th>
    </tr>
    <tr>
        <td style="vertical-align: top">longitude</td>
        <td style="vertical-align: top; word-wrap: break-word">The longitude of the location which is related to the given IP</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
    <tr>
        <td style="vertical-align: top">latitude</td>
        <td style="vertical-align: top; word-wrap: break-word">The latitude of the location which is related to the given IP</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
define stream IpStream(ip string); from IpStream#geo:geocoordinate(ip) select latitude, longitude insert into outputStream;
```
<p style="word-wrap: break-word">This will return the longitude and latitude of the given IPV4 or IPV6 address. So the results for the geocoordinate(95.31.18.119) are 55.7522, 37.6156</p>

### reversegeocode *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-function">(Stream Function)</a>*

<p style="word-wrap: break-word">This extension transforms a latitude and longitude coordinates into precise address information. The output contains string properties streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode and formattedAddress in order. However, these information are not available for all the geo coordinates. For example, if the latitude and longitude represent a place in a forest, only the high level information like country will be returned. For those which are not available, this extension will return "N/A" as the value.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
geo:reversegeocode(<DOUBLE> longitude, <DOUBLE> latitude)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">longitude</td>
        <td style="vertical-align: top; word-wrap: break-word">longitude value of the required location</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">DOUBLE</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">latitude</td>
        <td style="vertical-align: top; word-wrap: break-word">latitude value of the required location</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">DOUBLE</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
reversegeocode(6.909785, 79.852603)
```
<p style="word-wrap: break-word">This will return the precise address information of the given location. On this case this will return "27", "N/A", "Palm Grove", "Colombo", "Western Province",                        "Sri Lanka", "LK", "00300", "27 Palm Grove, Colombo 00300, Sri Lanka"</p>

