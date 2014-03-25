import csv
import random
from anneal import Annealer

geofields = [("FILEID",6,1),
("STUSAB",2,7),
("SUMLEV",3,9),
("GEOCOMP",2,12),
("CHARITER",3,14),
("CIFSN",2,17),
("LOGRECNO",7,19),
("REGION",1,26),
("DIVISION",1,27),
("STATE",2,28),
("COUNTY",3,30),
("COUNTYCC",2,33),
("COUNTYSC",2,35),
("COUSUB",5,37),
("COUSUBCC",2,42),
("COUSUBSC",2,44),
("PLACE",5,46),
("PLACECC",2,51),
("PLACESC",2,53),
("TRACT",6,55),
("BLKGRP",1,61),
("BLOCK",4,62)]
field_names = [x[0] for x in geofields]

def geo_records(fn):
	fp = open(fn)
	for row in fp:
		parsedrow = []
		for fieldname,fieldlen,fieldstart in geofields:
			parsedrow.append( row[fieldstart-1:fieldstart+fieldlen-1].strip() )	
		yield parsedrow

def find_logrecno(dirname, county,tract,block):
	# piece together the name of the file we need
	basename = dirname.split(".")[0]
	stateabbrev = basename[:2]
	year = basename[2:]
	fn = dirname+"/%sgeo%s.sf1"%(stateabbrev,year)

	county_ix = field_names.index("COUNTY")
	block_ix = field_names.index("BLOCK")
	tract_ix = field_names.index("TRACT")
	logrecno_ix = field_names.index("LOGRECNO")

	for i, rec in enumerate( geo_records(fn) ):
		if i%10000==0:
			print i

		if rec[county_ix] == county and rec[tract_ix]==tract and rec[block_ix]==block:
			return rec[logrecno_ix]

def parse_packinglist(dirname):
	# parses the packing file, returning (tablename, fileno, field_start, num_fields)

	packing_fn = dirname+"/"+dirname+".prd.packinglist.txt"

	fp=open(packing_fn)

	# hackily extract the machine-readable packing list
	sections = fp.read().split("\n\n")
	packinglist = sections[3].split("\n")
	packinglist = packinglist[:-1] # the last line is a row of meaninless "#####"s

	# parse each line in the packing list
	filecur = {}
	for item in packinglist:
		tablename, cellsaddr = item.split("|")[:-1]
		fileno, ncells = cellsaddr.split(":")
		fileno = int(fileno)
		ncells = int(ncells)

		if fileno not in filecur:
			filecur[fileno] = 0

		yield tablename, fileno, filecur[fileno], ncells

		filecur[fileno] = filecur[fileno]+ncells

def record(dirname,table,logrecno):
	N_HEADER_FIELDS = 5
	LOGRECNO_FIELD = 4

	pl = list(parse_packinglist(dirname))
	pl = dict([(tablename,(fileno,fieldstart,numfields)) for tablename,fileno,fieldstart,numfields in pl])

	# see which file, and where in the file, we need to look for the table we want
	fileno, fieldstart, numfields = pl[table]

	# piece together the name of the file we need
	basename = dirname.split(".")[0]
	stateabbrev = basename[:2]
	year = basename[2:]
	fn = "%s%05d%s.sf1"%(stateabbrev,fileno,year)

	# check every row if it's the record we want. If so, return the segment of the fields that hold the table
	rd = csv.reader(open(dirname+"/"+fn))
	for row in rd:
		if row[LOGRECNO_FIELD]==logrecno:
			return row[N_HEADER_FIELDS+fieldstart:N_HEADER_FIELDS+fieldstart+numfields]

class Person(object):
	def __init__(self, sex, age, white, black, asian, amerindian, otherrace, hispanic):
		self.sex = sex
		self.age = age
		self.white = white
		self.black = black
		self.asian = asian
		self.amerindian = amerindian
		self.hispanic = hispanic

	@classmethod
	def random(cls):
		sex = random.choice( ['male','female'] )
		age = random.randint( 0, 110 )
		white = random.choice( [True,False] )
		black = random.choice( [True,False] )
		asian = random.choice( [True,False] )
		amerindian = random.choice( [True,False] )
		otherrace = random.choice( [True,False] )
		hispanic = random.choice( [True,False] )

		return cls(sex,age,white,black,asian,amerindian,otherrace,hispanic)

	def randomize(self):
		self.sex = random.choice( ['male','female'] )
		self.age = random.randint( 0, 110 )
		self.white = random.choice( [True,False] )
		self.black = random.choice( [True,False] )
		self.asian = random.choice( [True,False] )
		self.amerindian = random.choice( [True,False] )
		self.therrace = random.choice( [True,False] )
		self.hispanic = random.choice( [True,False] )

	def __str__(self):
		return "<Person %s/%s>"%(self.sex,self.age)

def make_p12(people):
	males = filter(lambda x:x.sex=='male', people)
	females = filter(lambda x:x.sex=='female', people)

	ret = []
	ret.append( len(people) )
	ret.append( len(males) )
	ret.append( len([x for x in males if x.age<5]) )
	ret.append( len([x for x in males if x.age>=5 and x.age<=9]) )
	ret.append( len([x for x in males if x.age>=10 and x.age<=14]) )
	ret.append( len([x for x in males if x.age>=15 and x.age<=17]) )
	ret.append( len([x for x in males if x.age>=18 and x.age<=19]) )
	ret.append( len([x for x in males if x.age==20]) )
	ret.append( len([x for x in males if x.age==21]) )
	ret.append( len([x for x in males if x.age>=22 and x.age<=24]) )
	ret.append( len([x for x in males if x.age>=25 and x.age<=29]) )
	ret.append( len([x for x in males if x.age>=30 and x.age<=34]) )
	ret.append( len([x for x in males if x.age>=35 and x.age<=39]) )
	ret.append( len([x for x in males if x.age>=40 and x.age<=44]) )
	ret.append( len([x for x in males if x.age>=45 and x.age<=49]) )
	ret.append( len([x for x in males if x.age>=50 and x.age<=54]) )
	ret.append( len([x for x in males if x.age>=55 and x.age<=59]) )
	ret.append( len([x for x in males if x.age>=60 and x.age<=61]) )
	ret.append( len([x for x in males if x.age>=62 and x.age<=64]) )
	ret.append( len([x for x in males if x.age>=65 and x.age<=66]) )
	ret.append( len([x for x in males if x.age>=67 and x.age<=69]) )
	ret.append( len([x for x in males if x.age>=70 and x.age<=74]) )
	ret.append( len([x for x in males if x.age>=75 and x.age<=79]) )
	ret.append( len([x for x in males if x.age>=80 and x.age<=84]) )
	ret.append( len([x for x in males if x.age>=85]) )
	ret.append( len(females) )
	ret.append( len([x for x in females if x.age<5]) )
	ret.append( len([x for x in females if x.age>=5 and x.age<=9]) )
	ret.append( len([x for x in females if x.age>=10 and x.age<=14]) )
	ret.append( len([x for x in females if x.age>=15 and x.age<=17]) )
	ret.append( len([x for x in females if x.age>=18 and x.age<=19]) )
	ret.append( len([x for x in females if x.age==20]) )
	ret.append( len([x for x in females if x.age==21]) )
	ret.append( len([x for x in females if x.age>=22 and x.age<=24]) )
	ret.append( len([x for x in females if x.age>=25 and x.age<=29]) )
	ret.append( len([x for x in females if x.age>=30 and x.age<=34]) )
	ret.append( len([x for x in females if x.age>=35 and x.age<=39]) )
	ret.append( len([x for x in females if x.age>=40 and x.age<=44]) )
	ret.append( len([x for x in females if x.age>=45 and x.age<=49]) )
	ret.append( len([x for x in females if x.age>=50 and x.age<=54]) )
	ret.append( len([x for x in females if x.age>=55 and x.age<=59]) )
	ret.append( len([x for x in females if x.age>=60 and x.age<=61]) )
	ret.append( len([x for x in females if x.age>=62 and x.age<=64]) )
	ret.append( len([x for x in females if x.age>=65 and x.age<=66]) )
	ret.append( len([x for x in females if x.age>=67 and x.age<=69]) )
	ret.append( len([x for x in females if x.age>=70 and x.age<=74]) )
	ret.append( len([x for x in females if x.age>=75 and x.age<=79]) )
	ret.append( len([x for x in females if x.age>=80 and x.age<=84]) )
	ret.append( len([x for x in females if x.age>=85]) )

	return ret

def make_p14(people):
	people = filter(lambda x:x.age<20, people)

	males = filter(lambda x:x.sex=='male', people)
	females = filter(lambda x:x.sex=='female', people)

	ret = []
	ret.append( len(people) )

	ret.append( len(males) )
	for i in range(20):
		ret.append( len([x for x in males if x.age==i]) )

	ret.append( len(females) )
	for i in range(20):
		ret.append( len([x for x in females if x.age==i]) )

def diff_recs(r1,r2):
	return sum( [abs(v1-v2) for v1,v2 in zip(r1,r2)] )

def jitter_sex( people ):
	person = random.choice( people )
	if person.sex=='male':
		person.sex='female'
	else:
		person.sex='male'

def jitter_age( people ):
	person = random.choice( people )
	person.age = random.randint(0,110)

def jitter_underage( people ):
	people = filter(lambda x:x.age<20, people)

	person = random.choice(people)
	person.age = random.randint(person.age-5,person.age+5)

if __name__=='__main__':
	# print "searching for logrecno for block..."
	# county = "051"
	# tract = "007500"
	# block = "1021"
	# logrecno = find_logrecno( "or2010.sf1", county, tract, block )

	logrecno = "0159213"
	# establish some number of people
	p1 = int( record( "or2010.sf1", "p1", logrecno)[0] )

	p12 = [int(x) for x in record( "or2010.sf1", "p12", logrecno)]

	p14 = [int(x) for x in record( "or2010.sf1", "p14", logrecno)]

	def sex_diff(state):
		fake_p12 = make_p12(state)

		m_diff = abs(p12[1]-fake_p12[1])
		f_diff = abs(p12[25]-fake_p12[25])

		return m_diff+f_diff

	def p12_diff(state):
		fake_p12 = make_p12(state)
		return diff_recs(p12,fake_p12)

	def p14_diff(state):
		fake_p14 = make_p14(state)
		return diff_recs(p14,fake_p14)



	people = [Person.random() for x in range(p1)]

	print p12

	# anneal sex 
	annealer = Annealer(p12_diff, jitter_sex)
	state, e = annealer.anneal(people, 10000000, 0.01, 18000, 9)

	print make_p12(state)

	# anneal age	
	annealer = Annealer(p12_diff, jitter_age)
	state, e = annealer.anneal(state, 10000000, 0.01, 18000, 9)

	print make_p12(state)

	# anneal underage
	annealer = Annealer(p14_diff, jitter_underage)
	state, e = annealer.anneal(state, 10000000, 0.01, 18000, 9)

	print "p14:"
	print p14
	print make_p14(state)

