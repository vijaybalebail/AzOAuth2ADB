# Given the client ID and tenant ID for an app registered in Azure,
# provide a <ms-entra-id> access token and a refresh token.

# If the caller is not already signed in to Azure, the caller's
# web browser will prompt the caller to sign in first.

# pip install msal
from msal import PublicClientApplication
import sys

# You can hard-code the registered app's client ID and tenant ID here,
# or you can provide them as command-line arguments to this script.
client_id = 'f080bd12-c7d2-4acb-b8ab-5c8fa2cf5c40'
tenant_id = '5b743bc7-c1e2-4d46-b4b5-a32eddac0286'

# Do not modify this variable. It represents the programmatic ID for
# Enter the scope of DB registar scope url.
scopes = [ 'https://oracledevelopment.onmicrosoft.com/aa04583a-fa70-4bbd-8f62-ec4ec80213df/oracle_ADB_access' ]

# Check for too few or too many command-line arguments.
if (len(sys.argv) > 1) and (len(sys.argv) != 3):
  print("Usage: get-tokens.py <client ID> <tenant ID>")
  exit(1)

# If the registered app's client ID and tenant ID are provided as
# command-line variables, set them here.
if len(sys.argv) > 1:
  client_id = sys.argv[1]
  tenant_id = sys.argv[2]

app = PublicClientApplication(
  client_id = client_id,
  authority = "https://login.microsoftonline.com/" + tenant_id
)

acquire_tokens_result = app.acquire_token_interactive(
  scopes = scopes
)

if 'error' in acquire_tokens_result:
  print("Error: " + acquire_tokens_result['error'])
  print("Description: " + acquire_tokens_result['error_description'])
else:
  print("Access token:\n")
  print(acquire_tokens_result['access_token'])
  print("\nRefresh token:\n")
  print(acquire_tokens_result['refresh_token'])