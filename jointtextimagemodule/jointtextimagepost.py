import pandas as pd
import numpy as np



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


df = df.groupby(['obj'], as_index=False)['museum','deeplink','img','place_uri','place_country_code','text','time_uri','time_label','material_group','technique_group', 'technique_category','material_category','depict_group'].agg(lambda x: list(set(x)))

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
df['material_group'] = df['material_group'].astype(str).str.replace("'http://data.silknow.org/vocabulary/facet/animal_fibre',", "")
df['material_group'] = df['material_group'].astype(str).str.replace(", 'http://data.silknow.org/vocabulary/facet/animal_fibre'", "")
df['material_group'] = df['material_group'].astype(str).str.replace(", 'http://data.silknow.org/vocabulary/facet/animal_fibre',", ",")


df = df.reindex(columns=['obj','museum','text','img','deeplink','place_uri','place_country_code','time_uri','time_label','technique_group','technique_category','material_group','material_category','depict_group'])
df = df.sort_values(by=['museum'])

#this line removes the category columns
df = df.drop(columns=['technique_category', 'material_category'])

df.to_csv('total_post.csv')
