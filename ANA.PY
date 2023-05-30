import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import norm
from scipy.optimize import curve_fit

# Given data points
# data = [-47,-44,-54,-48,-48,-51,-55,-50,-48,-51,-56,-55,-50,-50,-47,-53,-49,-50,-56,-57,-59,-49,-47]
# data2 = [-72,-78,-63,-81,-72,-78,-68,-77,-69,-74,-77,-77,-81]

data= [-52,-53,-56,-56,-54,-56,-62,-58,-57,-55,-64,-58,-64,-58,-58,-61,-59,-60,-59,-61,-61,-57,-65,-67]
data2 = [-57,-49,-52,-50,-61,-52,-51,-48,-59,-49,-44,-52,-55,-59,-52,-50,-56,-51,-55,-49,-52,-60,-59,-58]

# Create a histogram
plt.hist(data, bins=10, density=True, alpha=0.5, label='Data1')
plt.hist(data2, bins=10, density=True, alpha=0.5, label='Data2')

# Fit a Gaussian curve to the histogram
(mu, sigma) = norm.fit(data)
(mu2, sigma2) = norm.fit(data2)

# Generate x values for the Gaussian curve
x = np.linspace(min(data), max(data), 100)
x2 = np.linspace(min(data2), max(data2), 100)

# Plot the Gaussian curve
plt.plot(x, norm.pdf(x, mu, sigma), 'r-', label='Gaussian Fit')
plt.plot(x2, norm.pdf(x2, mu2, sigma2), 'r-', label='Gaussian Fit')

# Add labels and legend to the plot
plt.xlabel('Value')
plt.ylabel('Frequency')
plt.title('Histogram with Gaussian Fit')
plt.legend()

# Show the plot
plt.show()