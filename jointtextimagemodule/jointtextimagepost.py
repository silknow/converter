import pandas as pd
import numpy as np
import os
import re

from urllib.parse import urlparse


df = pd.read_csv('total.csv', low_memory=False)






weave = 'twill|satin|tabby'
figured_weave = 'embroidery|velvet|damask|printed_fabric|damasse|lampas|patterned_fabric|brocatelle'
plain = 'cannele|metal_weft|rippled_effect_fabric|gauze'
weft = 'effect'
other_technique = 'other_technique'

paper = 'paper'
metal_silk = 'metal_thread|gold_thread|silver_thread|metal'
fibres = 'vegetal_fibre|animal_fibre|mixed_fibre|synthetic_fibre'
other = 'other_material'


conditions_tech = [
df['technique_group'].astype(str).str.contains(weave),
df['technique_group'].astype(str).str.contains(figured_weave),
df['technique_group'].astype(str).str.contains(plain),
df['technique_group'].astype(str).str.contains(weft),
df['technique_group'].astype(str).str.contains(other_technique)]
    
conditions_mat = [
df['material_group'].astype(str).str.contains(paper),
df['material_group'].astype(str).str.contains(metal_silk),
df['material_group'].astype(str).str.contains(fibres),
df['material_group'].astype(str).str.contains(other)]

choices_tech = ['weave', 'figured_weave','plain', 'weft', 'other_technique']
choices_mat = ['paper', 'metal_silk', 'fibre', 'other']

    
df['technique_category'] = np.select(conditions_tech, choices_tech)
df['material_category'] = np.select(conditions_mat, choices_mat)
df['technique_category'] = df['technique_category'].replace('0', np.nan)
df['material_category'] = df['material_category'].replace('0', np.nan)

df['category_group'] = df['type_a_group']

df['museum'] = df['museum'].apply(lambda x: urlparse(x).path.rsplit("/", 1)[-1])
df['technique_group'] = df['technique_group'].apply(lambda x: urlparse(x).path.rsplit("/", 1)[-1] if type(x) != float else x)
df['depict_group'] = df['depict_group'].apply(lambda x: urlparse(x).path.rsplit("/", 1)[-1] if type(x) != float else x)
df['category_group'] = df['category_group'].apply(lambda x: urlparse(x).path.rsplit("/", 1)[-1] if type(x) != float else x)
df['material_group'] = df['material_group'].apply(lambda x: urlparse(x).path.rsplit("/", 1)[-1] if type(x) != float else x)

df.groupby('museum')['museum'].value_counts().to_csv('museum_counts.csv')


df.groupby('museum')['material_group'].value_counts().to_csv('mat_counts.csv')
df.groupby('material_group')['material_group'].value_counts().to_csv('mat_total_counts.csv')
df.groupby('museum')['material_group'].value_counts(normalize=True).to_csv('mat_proportion.csv')

df.groupby('museum')['technique_group'].value_counts().to_csv('tec_counts.csv')
df.groupby('technique_group')['technique_group'].value_counts().to_csv('tec_total_counts.csv')
df.groupby('museum')['technique_group'].value_counts(normalize=True).to_csv('tec_proportion.csv')

df.groupby('museum')['depict_group'].value_counts().to_csv('dep_counts.csv')
df.groupby('museum')['depict_group'].value_counts(normalize=True).to_csv('dep_proportion.csv')

df.groupby('museum')['category_group'].value_counts().to_csv('category_counts.csv')
df.groupby('museum')['category_group'].value_counts(normalize=True).to_csv('category_proportion.csv')

df.groupby('museum')['time_label'].value_counts().to_csv('time_counts.csv')
df.groupby('time_label')['time_label'].value_counts().to_csv('time_total_counts.csv')
df.groupby('museum')['time_label'].value_counts(normalize=True).to_csv('time_proportion.csv')

df.groupby('museum')['place_country_code'].value_counts().to_csv('place_counts.csv')
df.groupby('place_country_code')['place_country_code'].value_counts().to_csv('place_total_counts.csv')
df.groupby('museum')['place_country_code'].value_counts(normalize=True).to_csv('place_proportion.csv')



df = df.groupby(['obj'], as_index=False)['museum','deeplink','img','place_uri','place_country_code','text','time_uri','time_label','material_group','technique_group', 'technique_category','material_category','depict_group','category_group'].agg(lambda x: list([i for i in x if pd.notna(i)]))

'''
df['technique_category'] = df['technique_category'].astype(str).str.replace("nan,", "")
df['technique_category'] = df['technique_category'].astype(str).str.replace(",nan", "")
df['technique_category'] = df['technique_category'].astype(str).str.replace(",nan,", ",")
df['technique_category'] = df['technique_category'].astype(str).str.replace(" ", "")
df['material_category'] = df['material_category'].astype(str).str.replace("nan,", "")
df['material_category'] = df['material_category'].astype(str).str.replace(",nan", "")
df['material_category'] = df['material_category'].astype(str).str.replace(",nan,", ",")
df['material_category'] = df['material_category'].astype(str).str.replace(" ", "")
'''

#these three lines remove the material group "animal_fibre", if it has other material groups, too
df['material_group'] = df['material_group'].astype(str).str.replace("'animal_fibre',", "")
df['material_group'] = df['material_group'].astype(str).str.replace(", 'animal_fibre'", "")
df['material_group'] = df['material_group'].astype(str).str.replace(", 'animal_fibre',", ",")




df = df.reindex(columns=['obj','museum','text','img','deeplink','place_uri','place_country_code','time_uri','time_label','technique_group','technique_category','material_group','material_category','depict_group','category_group'])
df = df.sort_values(by=['museum'])

#this line removes the category columns
df = df.drop(columns=['technique_category', 'material_category'])


df['museum'] = df['museum'].str[0]

df.to_csv('total_post.csv')

f = lambda x: x.to_csv(os.getcwd() + "/data_{}.csv".format(x.name.lower()), index=False)
df.groupby('museum').apply(f)


#count = 0
#for i, x in df.groupby('museum'):
#    count = count + 1
#    x.to_csv(str(count)+"_data.csv", index=False)
