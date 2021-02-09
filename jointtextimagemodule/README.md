# SILKNOW joint text image module
SPARQL query and post-processing script for the Joint Text Image module.


## Requirements
- Python (3) with Pandas and Numpy

## How to use
1. Run this [sparql query](https://github.com/silknow/converter/blob/master/jointtextimagemodule/total.sparql) on http://data.silknow.org/sparql and set the Results Format as CSV. Save the file as "total.csv"
2. Run the script with "python jointtextimagepost.py" in the same folder as "total.csv"
3. The main output is called "total_post.csv
4. In addition there are many more CSV files as output, most of them are just the same as the main output, but split by museum, and then there are "count" and "proportion" files which show more group statistics by museum.
