__author__ = 'shijianliu'

version = 1.0

try:
    import multiprocessing
except ImportError:
    pass

import os
import os.path
from setuptools import setup

os.chdir(os.path.dirname(os.path.abspath(__file__)))

dependencies = [
    'web.py',
    'requests',
    'ujson'
]

setup(name='hacker',
      version=version,
      description='Netease music hacker',
      author='liverliu',
      packages=['hacker'],
      scripts=["hacker/hacker.py"],
      install_requires=dependencies
)