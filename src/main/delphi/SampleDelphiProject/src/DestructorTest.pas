unit DestructorTest;

interface

type
  TTest = class(TObject)
  public
    destructor Destroy; override;
  end;

implementation

destructor TTest.Destroy;
begin
  FreeMyStuff;
  inherited;
end;