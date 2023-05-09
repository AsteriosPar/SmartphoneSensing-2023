#import pandas
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# Read data from csv file
data_jumping = pd.read_csv('data_activity/jumping.csv')
data_standing = pd.read_csv('data_activity/standing.csv')
data_walking = pd.read_csv('data_activity/walking.csv')

post_jumping_acc = []
post_walking_acc = []
post_standing_acc = []
post_jumping_gyro = []
post_walking_gyro = []
post_standing_gyro = []

post_jumping = data_jumping['rms_gyro'].mean()

for i in range(0, int(len(data_jumping)/10)):
    post_jumping_acc.append(data_jumping['rms_acc'][i*10:(i+1)*10].std())
    post_jumping_gyro.append(data_jumping['rms_gyro'][i*10:(i+1)*10].std())
    post_walking_acc.append(data_walking['rms_acc'][i*10:(i+1)*10].std())
    post_walking_gyro.append(data_walking['rms_gyro'][i*10:(i+1)*10].std())
    post_standing_acc.append(data_standing['rms_acc'][i*10:(i+1)*10].std())
    post_standing_gyro.append(data_standing['rms_gyro'][i*10:(i+1)*10].std())

# save to new csv file
df = pd.DataFrame({'acc_jumping': post_jumping_acc, 
                   'gyro_jumping': post_jumping_gyro,
                   'acc_walking': post_walking_acc,
                   'gyro_walking': post_walking_gyro,
                   'acc_standing': post_standing_acc,
                   'gyro_standing': post_standing_gyro})

# save df to csv file
df.to_csv('data_activity/trained.csv', index=False)


# # Plot data
# plt.plot(data_standing['rms_gyro'], label='x
# # plt.plot(data_jumping[''], label='y')
# # plt.plot(data_jumping['z'], label='z')
# plt.legend()
# plt.show()

