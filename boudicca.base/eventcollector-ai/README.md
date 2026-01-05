# Boudicca EventCollector AI

The goal of this module is to provide AI functionality for event collectors.
It is difficult to extract data from human readable text, but LLMs are a good candidate to solve this.

This module uses Spring AI to interface with well known LLMs, preferably using Mistral for now.

## Usage

Define MISTRALAI_API_KEY environment variable to use the module.

## Use Cases

- Extracting event dates
- Automatic Extraction from tags based on description text
- Automatic categorization based on enums
- Provide shortened description of the event
