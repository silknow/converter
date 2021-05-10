import pandas as pd
import numpy as np
import os
from urllib.parse import urlparse


filenames = ["depiction_visual_item.csv", "material.csv", "place.csv", "technique.csv", "time-span.csv"]
results = []


for i in filenames:
    print(i)
    df = pd.read_csv(i, low_memory=False)



    df = df.groupby(['obj'], as_index=False)['museum','label','text','img'].agg(lambda x: list(set(x)))

    numbers_of_rows = len(df.index)
    df['museum'] = df['museum'].str[0]
    museum_count = df.value_counts(subset=['museum'])
    print("number of objects: ",numbers_of_rows)
    print("number per museum: ",museum_count)








