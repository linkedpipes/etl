# Testing
Besides, unit test in the application itself, LinkedPipes ETL can also be tested using Python and user-based testing.

The Python test aim to test API functionality without the need for UI or user interaction.
You need python (at least 3.10) available. 
To run the test navigate to this directory and execute:
```shell
pip install -r requirements.txt
pytest ./src 
```

Scenarios for user testing are saved in the `scenarios` directory.
Unlike the previous group of test they require user interactions.
You can find list the scenarios bellow>
* [000 - Basics](./scenarios/000-basic.md)
