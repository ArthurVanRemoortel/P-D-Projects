package com.vub.pdproject.data.readers;


import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vub.pdproject.data.models.Business;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Sam van den Vonder
 */
public class YelpBusinessReader {
    // Most cities, except those containing a "-" or "." (or any other inconveniences that are not easily translated to an enum)
    public enum Cities {
        Scarborough, Westworld_Scottsdale, Median, Dunfermline, De_Forest, Seven_Hills, West_View, Sun_City_Grand,
        Chander, Monreoville, East_York, East_Liberty, Fairlawn, Farnham, Freiberg, Stoney_Creek, CHARLOTTE, Leith,
        Millvale, Pleasant_Hills, Euclid, Mount_Holly, Oakdale, Sun_Lakes, Painesville, Bedford_Heights,
        Sun_Prairie, Rexdale, Sheffield_Lake, Gorebridge, Mentor_On_the, Newbridge, Indianola, Independence,
        Bainbridge_Township, Wildwood, Woodbridge, Monongahela, DeForest, Side_Slopes, Mentor_On_the_Lake, Harwick,
        Imperial, Pepper_Pike, Inverkeithing, Mathews, Stoughton, Middlefield, Carnegie, Mc_Donald, Glasgow,
        Braddock, South_Las_Vegas, Scottsdale_AZ, Shadyside, Richmond_Hts, La_Prairie, Boucherville, Inverness,
        Kingussie, Ansnorveldt, Kirtland, Mccandless_Township, Beloeil, Sheffield, Don_Mills, Saint_Joseph, Dewey,
        Brooklyn, Gerlingen, Hemmingford, West_Elizabeth, McDonald, Las_Vegas_Strip, Elizabeth_Township, Cramond_Bridge,
        Brunswick_Hills, Mint__Hill, Kent, Napierville, Steinenbronn, Squirrel_Hill, Centre_Island, Geauga, Mayfield,
        Chagrin_Falls, Fithian, Kennedy_Township, Westlake, Mississauaga, Silver_Lake, Lowell, Montral, Westmount,
        Juniper_Green, Old_Town, Ditzingen, Highland_Heights, New_Eagle, Sagamore_Hills, N_E_Las_Vegas, Wolfschlugen,
        Crabtree, Glen_Williams, Highland_Hills, Strip_District, Plieningen, Berry, Joliette, Lachute, Ambridge,
        Elizabeth, Amherst, Jefferson_Hills, Toronto, Missisauga, Mcmurray, Rawdon, N_Las_Vegas, Phoneix,
        Esslingen_am_Neckar, W_Spring_Valley, Scarobrough, Maple_Grove, Aberlady, Concord_Township, Homestead,
        Phoenix_AZ, Baldwin, Gelndale, Litchfield, South_Amherst, WICKLIFFE, McFarland, Burntisland, Pineville,
        Enterprise, Canonsburg, Pencaitland, South_Park, North_Randall, Summerlin, Glenshaw, Plum, Alburg, Queensferry,
        Scarbrough, Warrensville, Youngtown, Charlemagne, Leinfelden, Markham, Macedonia, Piedmont, Norval, Library,
        West_Mifflin, North_Olmstead, St_Bernard_de_Lacolle, Madison, Bradford_West_Gwillimbury, Cuddy, Brookpark, Berg,
        Cuyahoga_Fls, Caledon_East, LasVegas, Weston, Edimbourg, New_Kensington, Lasswade, E_Gwillimbury, Mayfield_Hts,
        Indian_Trail, Livingston, Beachwood, Fort_Mill, Schomberg, Saint_Leonard, frazer, Crescent, Malton, Green_Tree,
        Houston, Bradford, Summerlin_South, Philo, San_Tan_Valley, City_of_Edinburgh, Bentleyville, Nellis_AFB, Aurora,
        Currie, McMasterville, Port_Credit, McKnight, Central_City_Village, Arlington, Mesa, Forest_Hills, East_Mesa,
        Rigaud, Beaconsfield, University_Ht, Monticello, Shaker_Hts, Hampton_Township, Sun_City, West_Toronto, Tempe,
        Indian_Hook, Ahwahtukee, Sheffield_Village, Northside, Champaign, Cramerton, Dallas, Willowdale, Tranent,
        Woodmere, Newbury, Lawrence, Mantua, Black_Earth, McAdenville, Roxboro, St_Leonard, Esslingen, Cooksville,
        Bathurst_Quay, Coraopolis, Central_City, Russellton, Middleburg_Heights, McCandless_Township, Berea,
        Neville_Island, Lyndhurst, Chateauguay, Downtown, Lakewood, Elrama, Edinburgh_City_of, Anthem, Waiblingen,
        Tolleson, Broadlands, Wilkins_Township, Stanley, Las_Vegas_NV, Brooklin, Cecil, Charlote, Banksville,
        Scaroborough, Warrensville_Hts, Bloomfield, Longueuil, University_Hts_, West_Homestead, Paoli,
        Mahomet, Edgeworth, Walton_Hills, Mount_Washington, Claremont, Middleburg_Hts, Newmarket, Glbert,
        Elk_Grove_Villa, Rankin, Bath, Savoy, Glendale, S_Concord, Hendersonville, Concord_Twp, Strongville,
        South_Euclid, Finleyville, Thornhil, Chambly, Las_Vegas, Buena_Vista, Pincourt, Verdun, Boisbriand, Brunswick,
        Lambton, University_Heights, Haddington, Dane, Las_Vegas_East, Tremont, Kleinburg, Churubusco, Sun_City_Anthem,
        Bonnyrigg, Ballantyne, Schwieberdingen, Chomedey, Willoughby, Boulder_City, Medina_Township,
        South_Park_Township, Cleveland_Hts, Hiram, Paradise_Valley, Homer, Verona, Swissvale, Ratho, Upper_Saint_Clair,
        Mississauga, Solon, Medina, Cuyahoga_Falls, North_Versailles, Peoria, Bay_Village, Ogden, Lachenaie,
        Chateau, South_Mountain, Arnold, N_W_Las_Vegas, Lorain, Hemmingen, Columbia_Station, Mansfield, Turtle_Creek,
        Chesterland, York, Windsor, Rural_Ridge, pleasant_hills, Ingram, Cleveland, Denkendorf, Lake_Wylie, Brossard,
        LaSalle, Godmanchester, Cramond, NORTH_YORK, Fitchburg, Concord, Mayfield_Village, AGINCOURT, Nord, Stouffville,
        Paw_Creek, Huntingdon, Munhall, Herminie, Fox_Chapel, Downsview, nboulder_city, Wilmerding, Champlain,
        Huntersville, Delson, Stow, Ingliston, Thornhill, Terrebonne, Longniddry, Hudson, Bonnyrigg_and_Lasswade,
        Vaughn, Higley, East_Cleveland, Paradise, North_York, Pheonix_AZ, Mercier, Rosemere, North_Huntingdon,
        Sun_City_West, Blackness, Bradfordwoods, Avondale, Bellvue, Allegheny, Ross_Township, Litchfield_Park,
        Huntsburg, Point_Claire, Ludwigsburg, Etobicoke, Willoughby_Hills, Frazer, Montreal, Henderson, Vimont,
        Loanhead, Caledon, Avalon, Mesa_AZ, Oakland, Montrose, Kernen_im_Remstal, East_Gwillimbury, Aichwald,
        Dravosburg, White_Oak, Noyan, North_Royalton, Chandler, Kirkland, Tega_Cay, Kannapolis, Willowick, Henderston,
        Brook_Park, Green_Valley, St_Joseph, McMurray, Garfield_Heights, Firth_of_Forth, Heidelberg, Lake_Park,
        Richmond_Heights, Blainville, East_Mc_Keesport, Scotland, Rillton, Fellbach, Elyria, W_Henderson, Weddington,
        East_Pittsburgh, Hendserson, Davidson, Scotesdale, Robinson, Wesley_Chapel, Richfield, Avon, Lake_Las_Vegas,
        Newhaven, Spring_Valley, Caledon_Village, Goodyear, Nobleton, Bernhausen, Mount_Albert, Nottingham,
        Leaside, Clairton, Lacolle, Lasalle, Vilas, Valley_View, Aberdour, Bedford_HTS, Harrisbug, Inglewood,
        Pittsburch, Stowe_Township, Georgetown, Auburn_Township, Edgewood, Kahnawake, Ahwatukee_Foothills_Village,
        Kirkcaldy, las_vegas, Gibsonia, Laval, Pheonix, Oakville, Hawthorne, oakville, Parma, Parma_Heights, Gilbert,
        Chardon, Filderstadt, Blue_Diamond, TORONTO, N_Ridgeville, Mt_Lebanon, St_Philippe_de_Laprairie, Harrisburg,
        Ross, MC_Murray, Unionville, Mirabel, Franklin_Park, Remseck_am_Neckar, Waunakee, San_Tan, Ostfildern, Broxburn,
        Cheswick, North_Ridgeville, Ranlo, Cross_Plains, Ormstown, Mt_Albert, Penicuik, Mesa_Arizona, Blawnox, Dormont,
        Pointe_Claire, Oakmont, Mint_Hill, Saint_Laurent, Laveen, Stowe, Cleveland_Heights, Pittsburgh, Kernen,
        Repentigny, Peninsula, Streetsville, Auburn, Sidney, Gullane, Brookline, Stallings, Bolton, Avon_Lake, MESA,
        Trafford, Irwin, Burton, Regent_Square, Leetsdale, Scottsadale, mesa, Leslieville, Middleton, McMurry,
        Edinburgh, Wilkinsburg, Indian_Land, Oka, Quartermile, Belleville, Venetia, Munroe_Falls, Urbana,
        Oakwood_Village, Las_vegas, Whitney, Pierrefonds, East_Gwilimbury, Bedford, Reminderville, Remseck,
        Shaler_Township, West_Bridgewater, Aspinwall, North_Olmsted, Fountainbridge, Waddell, Nellis_Afb,
        Bridgeville, Russell_Twp, Greenfield_Park, La_Salle, Fairport_Harbor, Glassport, Dalgety_Bay, Chertsey,
        Wickliffe, Presto, Midland, Valley_City, West_Las_Vegas, Scott_Township, North_Queensferry, Broadview_Heights,
        London, Tolono, Boston_Heights, Warrensvile_Heights, Guadalupe, Monona, Gormley, Southside_Flats,
        Mount_Royal, Shady_Side, Pickering, Nellis_Air_Force_Base, Monroeville, Maple_Heights, South_Queensferry,
        Upper_St_Clair, Kettleby, Ajax, Springdale, North_Toronto, Tuscola, Cowdenbeath, Wexford, South_Hills,
        Madison_WI, Carefree, Concord_Mills, Municipality_of_Murrysville, Kornwestheim, Richmond_Hill, Twinsburg,
        Emsworth, Hinckley, Clark_county, Sunrise, Charlotte, Thorncliffe_Park, Penn_Hills, Chatauguay, Ahwatukee,
        Shorewood_Hills, Gifford, Shaker_Heights, Newington, SCOTTSDALE_AZ, Bainbridge, Downtown_Toronto, Mc_Murray,
        Châteauguay, Bethel_Park, Copley, Bruntsfield, Brentwood, Portobello, Warrensville_Heights, Rocky_River,
        McKees_Rocks, Glenndale, Moon_Twp, Fairview_Park, columbia_station, Water_of_Leith, Dorval, Schwaikheim, Mentor,
        Sindelfingen, Moreland_Hills, Liberty, Tottenham, Newburgh_Heights, Cuyahoga_Heights, Sunnyslope,
        Laveen_Village, Mississuaga, Phoenix, Northfield, Strongsville, Olmsted_Township, Straiton, McKeesport,
        Rouses_Point, Etna, Mount_Horeb, Stockbridge, Eastlake, Mooers, Cave_Creek, Gates_Mills, Pitcairn, Streetsboro,
        Fitchburgh, Moon, Sloan, Belmont, Grafton, Saint_Anns, Orange, Gastonia, Glenrothes, Candiac, Mattews, Waxhaw,
        W_Summerlin, El_Mirage, etobicoke, McCandless, Roslin, Sharpsburg, Surprise, Brampton, Creighton, Kirknewton,
        New_Town, Palmerston, Lagrange, Robinson_Township, Whitby, Lowesville, Rantoul, Mc_Farland, Goodwood,
        Spring_Hill_City_View, Fort_McDowell, Lachine, West_Akron, Scottdale, Cornelius, Northfield_Center,
        Centennial_Hills, SLOAN, Monoroeville, North_Las_Vegas, South_Gyle, Fountain_Hills, Crafton, Fairview, King,
        Balerno, Roosevelt, East_McKeesport, Dalkeith, Novelty, Leonberg, Anjou, Vaughan, Grand_River, Murrysville,
        Sewickley, Prestonpans, Midlothian, East_Credit, King_City, Matthews, Oregon, Lower_Burrell, Las_Vegass,
        Olmsted_Falls, Bratenahl, Stuttgart, Villa_Grove, Lothian, Cottage_Grove, Castle_Shannon, Musselburgh,
        Moon_Township, Clarkson, Mayfield_Heights, Harmarville, Northfield_Center_Township, Allison_Park, Maple,
        Mascouche, Tallmadge, Morriston, Bellevue, Outremont, Lower_Lawrenceville, Beeton, Ben_Avon, Brecksville,
        Scottsdale, Fabreville, Richmond_Hil, Mount_Lebanon
    }

    /**
     * Read a file of Yelp business data and return in-memory objects mapped to their ID
     *
     * @param dataPath Path to the Yelp JSON business data, e.g. "data/yelp_academic_dataset_business.json"
     * @return A mapping from business id's to businesses
     */
    static public Map<String, Business> readData(String dataPath) throws IOException {
        // Read all businesses
        InputStream dataStream = getInputStream(dataPath);
        return readJsonStream(dataStream);
    }

    /**
     * Read the data for all businesses in a particular city
     *
     * @param dataPath Path to the yelp JSON business data
     * @param city     A city
     * @return A mapping from business id's to businesses
     */
    static public Map<String, Business> readCityData(String dataPath, Cities city) throws IOException {
        InputStream dataStream = getInputStream(dataPath);
        String cityString = getCityString(city);
        return readJsonStream(dataStream, business -> business.city.equals(cityString));
    }

    /**
     * @param inputStream   An stream of data
     * @param howMany       How many businesses should be read?
     * @return A mapping of ids and businesses, converted from entries on the stream of data
     */
    static private HashMap<String, Business> readJsonStream(InputStream inputStream) throws IOException {
        return readJsonStream(inputStream, business -> true);
    }

    /**
     * @param inputStream   An stream of data
     * @param shouldInclude Lambda to test whether the business should be included in the resultset or not
     * @return A mapping of ids and businesses, converted from entries on the stream of data
     */
    static private HashMap<String, Business> readJsonStream(InputStream inputStream, Function<Business, Boolean> shouldInclude) throws IOException {
        Gson gson = createBusinessBuilder();
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        JsonStreamParser parser = new JsonStreamParser(reader);

        HashMap<String, Business> businesses = new HashMap<>();
        while (parser.hasNext()) {
            JsonElement e = parser.next();
            if (e.isJsonObject()) {
                // Map json to a business object
                Business business = gson.fromJson(e, Business.class);
                if (shouldInclude.apply(business)) {
                    businesses.put(business.id, business);
                }
            }
        }
        reader.close();
        return businesses;
    }


    /**
     * Create a builder for JSON data
     *
     * @return a GSON builder for JSON data
     */
    static private Gson createBusinessBuilder() {
        GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }

    /**
     * Given a path to a file, turn it into a stream of data
     *
     * @param dataPath Path to a file
     * @return A stream of data
     */
    static private InputStream getInputStream(String dataPath) throws FileNotFoundException {
        return new FileInputStream(dataPath);
    }

    static private String getCityString(Cities city) {
        return city.toString().replaceAll("_", " ");
    }
}