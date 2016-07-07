import random
def main():
	f = random.choice([True, False])
	if(f):
		result.value = random.uniform(40, 400);
		result.msg = "Test"
		result.status = result.STATUS_GOOD
	else:
		result.value = float('nan')
		result.msg = "Bugger"
		result.status = result.STATUS_ERROR