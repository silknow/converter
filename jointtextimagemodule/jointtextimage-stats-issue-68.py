import pandas as pd
import numpy as np
import os
from urllib.parse import urlparse


filenames = ["depiction.csv", "material.csv", "place.csv", "technique.csv", "time.csv"]
results = []


for i in filenames:
    print(i)
    df = pd.read_csv(i, low_memory=False)



    df = df.groupby(['o'], as_index=False)['g','l','c','image'].agg(lambda x: list(set(x)))

    numbers_of_rows = len(df.index)
    df['g'] = df['g'].str[0]
    museum_count = df.value_counts(subset=['g'])
    print("number of objects: ",numbers_of_rows)
    print("number per museum: ",museum_count)








